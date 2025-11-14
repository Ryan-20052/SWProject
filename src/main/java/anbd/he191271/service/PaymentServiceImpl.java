package anbd.he191271.service;

import anbd.he191271.config.VNPAYConfig;
import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Order;
import anbd.he191271.entity.OrderDetail;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.OrderRepository;
import anbd.he191271.repository.VariantRepository;
import anbd.he191271.util.VNPAYUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final VariantRepository variantRepo;
    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final CustomerRepository customerRepo;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VNPAYConfig vnpConfig;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO, String clientIp) throws Exception {
        // 1) Tạo đơn PENDING
        Order order = new Order();
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));

        // Liên kết Customer
        if (requestDTO.getCustomerId() != null) {
            Customer customer = customerRepo.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + requestDTO.getCustomerId()));
            order.setCustomer(customer);
        } else {
            throw new IllegalArgumentException("Customer ID is required");
        }

        order = orderRepo.save(order);

        // 2) Tính tiền từ variants
        long total = 0L;
        for (PaymentRequestDTO.Item it : requestDTO.getItems()) {
            Variant v = variantRepo.findById(it.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + it.getVariantId()));
            int qty = it.getQuantity() != null ? it.getQuantity() : 1;
            total += v.getPrice() * qty;

            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setVariant(v);
            od.setAmount(qty);
            orderDetailRepo.save(od);
        }

        // 3️⃣ Áp dụng voucher (nếu có)
        double finalTotal = total;
        long discountAmount = 0L;
        if (requestDTO.getVoucherCode() != null && !requestDTO.getVoucherCode().isEmpty()) {
            try {
                double newTotal = voucherService.applyVoucher(requestDTO.getVoucherCode(), total);
                discountAmount = total - Math.round(newTotal);
                finalTotal = newTotal;
                order.setVoucherCode(requestDTO.getVoucherCode());
                order.setDiscountAmount(discountAmount);
            } catch (Exception e) {
                System.err.println("⚠️ Voucher không hợp lệ: " + e.getMessage());
            }
        }

        order.setTotalAmount(Math.round(finalTotal));
        orderRepo.save(order);

        // 4️⃣ Build tham số VNPAY
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(order.getTotalAmount() * 100)); // dùng tổng sau giảm
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", order.getCode());
        vnp_Params.put("vnp_OrderInfo", requestDTO.getOrderInfo() != null
                ? requestDTO.getOrderInfo()
                : ("Thanh toan don #" + order.getCode()));
        vnp_Params.put("vnp_OrderType", requestDTO.getOrderType() != null ? requestDTO.getOrderType() : "other");
        vnp_Params.put("vnp_Locale", requestDTO.getLocale() != null ? requestDTO.getLocale() : "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", extractClientIp(clientIp));

        ZoneId tz = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now(tz).format(fmt));
        vnp_Params.put("vnp_ExpireDate", LocalDateTime.now(tz).plusMinutes(15).format(fmt));

        String paymentUrl = VNPAYUtil.getPaymentURL(vnp_Params, vnpConfig.getPayUrl(), vnpConfig.getHashSecret());

        return new PaymentResponseDTO("00", "Success", paymentUrl,
                order.getId(), order.getCode(), order.getTotalAmount(),
                order.getCustomer().getUsername(),
                order.getCustomer().getEmail());
    }

    @Override
    public PaymentResponseDTO handleReturn(Map<String, String> params) throws Exception {
        boolean ok = VNPAYUtil.validateSignature(params, vnpConfig.getHashSecret());
        if (!ok) return new PaymentResponseDTO("97", "Sai chữ ký", null, null, null, null, null, null);

        String rsp = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        Order order = orderRepo.findByCode(txnRef).orElse(null);
        if (order == null) return new PaymentResponseDTO("01", "Order not found", null, null, null, null, null, null);

        if ("00".equals(rsp)) {
            // ✅ Thanh toán thành công
            if (!"PAID".equals(order.getStatus())) {
                order.setStatus("PAID");
                orderRepo.save(order);


            }

            return new PaymentResponseDTO("00", "Thanh toán thành công", null,
                    order.getId(), order.getCode(), order.getTotalAmount(),
                    order.getCustomer().getUsername(),
                    order.getCustomer().getEmail());
        }
        return new PaymentResponseDTO(rsp, "Thanh toán thất bại", null,
                order.getId(), order.getCode(), order.getTotalAmount(),
                order.getCustomer().getUsername(),
                order.getCustomer().getEmail());
    }

    @Override
    @Transactional
    public PaymentResponseDTO handleIpn(Map<String, String> params) throws Exception {
        boolean ok = VNPAYUtil.validateSignature(params, vnpConfig.getHashSecret());
        if (!ok) {
            return new PaymentResponseDTO("97", "Invalid signature", null, null, null, null, null, null);
        }

        String txnRef = params.get("vnp_TxnRef");
        String rsp = params.get("vnp_ResponseCode");
        long amountXu = Long.parseLong(params.get("vnp_Amount"));

        Order order = orderRepo.findByCode(txnRef).orElse(null);
        if (order == null) {
            return new PaymentResponseDTO("01", "Order not found", null, null, null, null, null, null);
        }

        if (amountXu != order.getTotalAmount() * 100) {
            return new PaymentResponseDTO("04", "Invalid amount", null,
                    order.getId(), order.getCode(), order.getTotalAmount(),
                    order.getCustomer().getUsername(),
                    order.getCustomer().getEmail());
        }

        if ("00".equals(rsp)) {
            if (!"PAID".equals(order.getStatus())) {
                order.setStatus("PAID");
                orderRepo.save(order);
                // ✅ Nếu đơn có dùng voucher thì trừ lượt sử dụng
                if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
                    try {
                        voucherService.decreaseUsage(order.getVoucherCode());
                        System.out.println("🎟️ Voucher " + order.getVoucherCode() + " đã được trừ 1 lượt sau thanh toán.");
                    } catch (Exception e) {
                        System.err.println("⚠️ Không thể trừ lượt voucher: " + e.getMessage());
                    }
                }
            }
            return new PaymentResponseDTO("00", "Confirm Success", null,
                    order.getId(), order.getCode(), order.getTotalAmount(),
                    order.getCustomer().getUsername(),
                    order.getCustomer().getEmail());
        } else {
            order.setStatus("FAILED");
            orderRepo.save(order);
            return new PaymentResponseDTO("00", "Confirm Failed", null,
                    order.getId(), order.getCode(), order.getTotalAmount(),
                    order.getCustomer().getUsername(),
                    order.getCustomer().getEmail());
        }
    }

    private String extractClientIp(String remoteAddr) {
        return remoteAddr;
    }
    @Transactional
    public void updateOrderStatus(String orderCode, String newStatus) {
        Order order = orderRepo.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        order.setStatus(newStatus);
        orderRepo.save(order);
    }
}