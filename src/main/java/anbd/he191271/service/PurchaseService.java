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
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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
    private ShoppingCartRepository cartRepo; // 👈 inject repo, đặt tên biến khác tên class

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

    private Date calculateExpiredDate(String duration, Date startDate) {
        if (duration == null || duration.isEmpty()) {
            return new Date(startDate.getTime() + (24L * 60 * 60 * 1000));
        }
        String lower = duration.toLowerCase().trim();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        try {
            int value = Integer.parseInt(lower.replaceAll("[^0-9]", ""));
            if (lower.contains("day") || lower.contains("ngày")) {
                cal.add(Calendar.DAY_OF_MONTH, value);
            } else if (lower.contains("month") || lower.contains("tháng")) {
                cal.add(Calendar.MONTH, value);
            } else if (lower.contains("year") || lower.contains("năm")) {
                cal.add(Calendar.YEAR, value);
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTime();
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
            order = orderRepository.findByCodeWithCustomer(orderCode)
                    .orElseThrow(() -> new Exception("Không tìm thấy đơn hàng hoặc khách hàng"));
        }

        order.setStatus("PAID");
        orderRepository.save(order);

        for (OrderDetail detail : order.getOrderDetails()) {
            Variant variant = detail.getVariant();
            Date activatedAt = new Date();

            for (int i = 0; i < detail.getAmount(); i++) {
                String licenseKey = generateKey(16);
                Date expiredAt = calculateExpiredDate(variant.getDuration(), activatedAt);

                LicenseKey key = new LicenseKey();
                key.setKey(licenseKey);
                key.setOrderDetail(detail);
                key.setActivatedAt(activatedAt);
                key.setExpiredAt(expiredAt);
                key.setStatus("ACTIVATE");
                licenseKeyRepository.save(key);

                String emailContent = """
                <div style="font-family: Arial, sans-serif; color: #333;">
                    <h2 style="color: #2E86DE;">Thanh toán thành công!</h2>
                    <p><b>Khách hàng:</b> %s</p>
                    <p><b>Sản phẩm:</b> %s</p>
                    <p><b>Mã đơn hàng:</b> %s</p>
                    <hr>
                    <p><b>License key của bạn:</b> 
                        <span style="font-size:16px; color:#e74c3c; font-weight:bold;">%s</span>
                    </p>
                    <p><b>Thời hạn:</b> %s → %s</p>
                    <br>
                    <p>Cảm ơn bạn đã mua sản phẩm tại <b>License Shop</b> ❤️</p>
                </div>
                """.formatted(
                        order.getCustomer().getName(),
                        variant.getProduct().getName(),
                        order.getCode(),
                        licenseKey,
                        activatedAt,
                        expiredAt
                );

                mailService.sendEmail(
                        order.getCustomer().getEmail(),
                        "License Key của bạn - License Shop",
                        emailContent
                );

                String hashedKey = hashSHA256(licenseKey);
                key.setKey(hashedKey);
                licenseKeyRepository.save(key);
            }

            // ✅ gọi qua bean instance, không gọi static
            cartRepo.deleteByCustomerIdAndVariant_Id(
                    Long.valueOf(order.getCustomer().getId()),
                    Long.valueOf(detail.getVariant().getId())
            );
        }
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
