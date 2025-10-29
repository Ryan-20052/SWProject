package anbd.he191271.service;

import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.LicenseKeyRepository;
import anbd.he191271.util.HashUtil;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled; // THÊM IMPORT
import org.springframework.stereotype.Service;

import java.util.Date; // THÊM IMPORT
import java.util.List;
import java.util.Optional;

@Service
public class LicenseService {

    private final LicenseKeyRepository repo;

    public LicenseService(LicenseKeyRepository repo) {
        this.repo = repo;
    }

    /**
     * Kiểm tra key kèm theo email customer
     */
    public VerificationResult verifyPlainKeyForCustomer(String plainKey, String email) {
        String hashed = HashUtil.sha256Hex(plainKey);
        Optional<LicenseKey> opt = repo.findByKey(hashed);
        if (opt.isEmpty()) {
            return VerificationResult.invalid("Key không đúng");
        }

        LicenseKey lk = opt.get();

        // THÊM KIỂM TRA TRẠNG THÁI EXPIRED
        if ("EXPIRED".equalsIgnoreCase(lk.getStatus())) {
            return VerificationResult.invalid("Key đã hết hạn");
        }

        if (!"ACTIVATE".equalsIgnoreCase(lk.getStatus())) {
            return VerificationResult.invalid("Key chưa active hoặc không hợp lệ");
        }

        Date expired = lk.getExpiredAt();
        if (expired == null) {
            return VerificationResult.invalid("Key không có ngày hết hạn");
        }
        if (new Date().after(expired)) {
            // TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI KHI PHÁT HIỆN HẾT HẠN
            lk.setStatus("EXPIRED");
            repo.save(lk);
            return VerificationResult.invalid("Key đã hết hạn");
        }

        if (lk.getOrderDetail() == null
                || lk.getOrderDetail().getOrder() == null
                || lk.getOrderDetail().getOrder().getCustomer() == null) {
            return VerificationResult.invalid("Không xác định được chủ sở hữu của key");
        }

        String ownerEmail = lk.getOrderDetail().getOrder().getCustomer().getEmail();
        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(email)) {
            return VerificationResult.invalid("License key không thuộc về tài khoản " + email);
        }

        String customerName = lk.getOrderDetail().getOrder().getCustomer().getName();
        String productName = lk.getOrderDetail().getVariant().getName();
        return VerificationResult.ok(expired, customerName, productName);
    }

    /**
     * Cập nhật trạng thái license hết hạn - THÊM METHOD NÀY
     */
    @Transactional
    public void updateExpiredLicenses() {
        List<LicenseKey> expiredLicenses = repo.findByStatusAndExpiredAtBefore("ACTIVATE", new Date());

        for (LicenseKey license : expiredLicenses) {
            license.setStatus("EXPIRED");
        }

        repo.saveAll(expiredLicenses);
    }

    /**
     * Tự động chạy mỗi ngày lúc 00:00 để cập nhật license hết hạn - THÊM METHOD NÀY
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void scheduledUpdateExpiredLicenses() {
        updateExpiredLicenses();
    }

    // CÁC METHOD CŨ GIỮ NGUYÊN
    public static class VerificationResult {
        private final boolean ok;
        private final String message;
        private final Date expiredAt;
        private final String customerName;
        private final String productName;

        private VerificationResult(boolean ok, String message, Date expiredAt, String customerName, String productName) {
            this.ok = ok;
            this.message = message;
            this.expiredAt = expiredAt;
            this.customerName = customerName;
            this.productName = productName;
        }

        public static VerificationResult ok(Date expiredAt, String customerName, String productName) {
            return new VerificationResult(true, "OK", expiredAt, customerName, productName);
        }

        public static VerificationResult invalid(String message) {
            return new VerificationResult(false, message, null, null, null);
        }

        public boolean isOk() { return ok; }
        public String getMessage() { return message; }
        public Date getExpiredAt() { return expiredAt; }
        public String getCustomerName() { return customerName; }
        public String getProductName() { return productName; }
    }

    public List<LicenseKey> getAllLicense(){
        return repo.findAll();
    }

    public LicenseKey getLicenseById(int id) {
        return repo.findById(id).get();
    }

    public void save(LicenseKey lk) {
        repo.save(lk);
    }

    public void  delete(LicenseKey lk) {
        repo.delete(lk);
    }
}