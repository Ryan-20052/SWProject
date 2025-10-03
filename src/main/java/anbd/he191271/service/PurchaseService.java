package anbd.he191271.service;

import anbd.he191271.entity.*;
import anbd.he191271.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

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

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Generate license key random
    private String generateKey(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            key.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return key.toString();
    }

    // ✅ Hàm tính ngày hết hạn từ duration (support day / month / year)
    // ✅ Hàm tính ngày hết hạn từ duration (support day / month / year + tiếng Việt)
    private Date calculateExpiredDate(String duration, Date startDate) {
        if (duration == null || duration.isEmpty()) {
            return new Date(startDate.getTime() + (24L * 60 * 60 * 1000)); // fallback +1 ngày
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
                cal.add(Calendar.DAY_OF_MONTH, 1); // fallback
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi parse duration: " + duration + " -> fallback +1 ngày");
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return cal.getTime();
    }


    @Transactional
    public void purchaseVariant(int variantId, int customerId, String customerEmail, int amount) throws Exception {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new Exception("Variant not found"));

        // 1. Tạo Order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(new Date());
        orderRepository.save(order);

        // 2. Tạo OrderDetail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setVariant(variant);
        detail.setAmount(amount);
        orderDetailRepository.save(detail);

        // 3. Generate License Key
        String licenseKey = generateKey(16);

        // 4. Gửi mail
        mailService.sendEmail(customerEmail,
                "Your License Key",
                "<h2>Cảm ơn bạn đã mua hàng tại License Shop!</h2>" +
                        "<p><b>License key của bạn:</b> " + licenseKey + "</p>" +
                        "<p>Vui lòng giữ key cẩn thận, và kích hoạt trong thời gian hiệu lực.</p>");

        // 5. Lưu LicenseKey
        LicenseKey key = new LicenseKey();
        key.setKey(licenseKey);
        key.setOrderDetail(detail);

        Date activatedAt = new Date();
        key.setActivatedAt(activatedAt);

        // ✅ Tính ngày hết hạn chuẩn
        Date expiredAt = calculateExpiredDate(variant.getDuration(), activatedAt);
        key.setExpiredAt(expiredAt);

        licenseKeyRepository.save(key);

        // 6. (Option) Trừ stock nếu có
    }
}
