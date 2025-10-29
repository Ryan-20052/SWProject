package anbd.he191271.service;

import anbd.he191271.entity.*;
import anbd.he191271.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PurchaseService {

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    @Autowired
    private EmailService mailService;

    @Autowired
    private ShoppingCartRepository cartRepo;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateKey(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            key.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return key.toString();
    }

    private LocalDateTime calculateExpiredDate(String duration, LocalDateTime startDate) {
        if (duration == null || duration.isEmpty()) {
            return startDate.plusDays(1);
        }

        String lower = duration.toLowerCase().trim();

        try {
            int value = Integer.parseInt(lower.replaceAll("[^0-9]", ""));
            if (lower.contains("day") || lower.contains("ngày")) {
                return startDate.plusDays(value);
            } else if (lower.contains("month") || lower.contains("tháng")) {
                return startDate.plusMonths(value);
            } else if (lower.contains("year") || lower.contains("năm")) {
                return startDate.plusYears(value);
            } else {
                return startDate.plusDays(1);
            }
        } catch (Exception e) {
            return startDate.plusDays(1);
        }
    }

    @Transactional
    public Order createPendingOrder(Customer customer, int variantId, int amount) throws Exception {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new Exception("Không tìm thấy gói sản phẩm"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setCode(generateOrderCode());
        orderRepository.save(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setVariant(variant);
        detail.setAmount(amount);
        orderDetailRepository.save(detail);

        return order;
    }

    @Transactional
    public void processSuccessfulPayment(String orderCode) throws Exception {
        Order order = orderRepository.findByCodeWithCustomer(orderCode)
                .orElseThrow(() -> new Exception("Không tìm thấy đơn hàng với mã: " + orderCode));

        if (order.getCustomer() == null) {
            throw new Exception("Không tìm thấy khách hàng cho đơn hàng: " + orderCode);
        }

        order.setStatus("PAID");
        orderRepository.save(order);

        // 📩 Gom nội dung email
        StringBuilder licenseList = new StringBuilder();
        List<LicenseKey> allKeys = new ArrayList<>();

        for (OrderDetail detail : order.getOrderDetails()) {
            Variant variant = detail.getVariant();
            LocalDateTime activatedAt = LocalDateTime.now();

            for (int i = 0; i < detail.getAmount(); i++) {
                String rawKey = generateKey(16);
                LocalDateTime expiredAt = calculateExpiredDate(variant.getDuration(), activatedAt);

                // Lưu key (hash vào DB)
                LicenseKey key = new LicenseKey();
                key.setKey(hashSHA256(rawKey));
                key.setOrderDetail(detail);
                key.setActivatedAt(activatedAt);
                key.setExpiredAt(expiredAt);
                key.setStatus("ACTIVATE");
                allKeys.add(key);

                // ✅ Thêm vào bảng email: Product + Duration
                licenseList.append("""
                <tr>
                    <td style="padding:6px; border:1px solid #ccc;">%s - %s</td>
                    <td style="padding:6px; border:1px solid #ccc; color:#e74c3c; font-weight:bold;">%s</td>
                    <td style="padding:6px; border:1px solid #ccc;">%s → %s</td>
                </tr>
            """.formatted(
                        variant.getProduct().getName(),   // Tên sản phẩm
                        variant.getDuration(),            // Thời hạn gói
                        rawKey,                           // License key thật
                        activatedAt,                      // Ngày bắt đầu
                        expiredAt                         // Ngày hết hạn
                ));
            }

            // ✅ Xóa item khỏi giỏ hàng
            cartRepo.deleteByCustomerIdAndVariant_Id(
                    Long.valueOf(order.getCustomer().getId()),
                    Long.valueOf(variant.getId())
            );
        }

        // ✅ Batch save tất cả key
        licenseKeyRepository.saveAll(allKeys);

        // 📧 Gửi email 1 lần duy nhất
        String emailContent = """
        <div style="font-family: Arial, sans-serif; color: #333;">
            <h2 style="color: #2E86DE;">Thanh toán thành công!</h2>
            <p><b>Khách hàng:</b> %s</p>
            <p><b>Mã đơn hàng:</b> %s</p>
            <hr>
            <table style="border-collapse: collapse; width:100%%; margin-top:10px;">
                <thead>
                    <tr style="background:#2563eb; color:white; text-align:left;">
                        <th style="padding:8px; border:1px solid #ccc;">Gói/Variant</th>
                        <th style="padding:8px; border:1px solid #ccc;">License Key</th>
                        <th style="padding:8px; border:1px solid #ccc;">Thời hạn</th>
                    </tr>
                </thead>
                <tbody>
                    %s
                </tbody>
            </table>
            <br>
            <p>Cảm ơn bạn đã mua sản phẩm tại <b>License Shop</b> ❤️</p>
        </div>
    """.formatted(
                order.getCustomer().getName(),
                order.getCode(),
                licenseList.toString()
        );

        mailService.sendEmail(
                order.getCustomer().getEmail(),
                "License Key của bạn - License Shop",
                emailContent
        );
    }

    private String hashSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}