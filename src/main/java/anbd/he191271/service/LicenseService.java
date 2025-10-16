package anbd.he191271.service;

import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.LicenseKeyRepository;
import anbd.he191271.util.HashUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
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

        // 🔹 Kiểm tra trạng thái
        if (!"ACTIVATE".equalsIgnoreCase(lk.getStatus())) {
            return VerificationResult.invalid("Key chưa active hoặc không hợp lệ");
        }

        // 🔹 Kiểm tra hết hạn
        Date expired = lk.getExpiredAt();
        if (expired == null) {
            return VerificationResult.invalid("Key không có ngày hết hạn");
        }
        if (new Date().after(expired)) {
            return VerificationResult.invalid("Key đã hết hạn");
        }

        // 🔹 Kiểm tra chủ sở hữu
        if (lk.getOrderDetail() == null
                || lk.getOrderDetail().getOrder() == null
                || lk.getOrderDetail().getOrder().getCustomer() == null) {
            return VerificationResult.invalid("Không xác định được chủ sở hữu của key");
        }

        String ownerEmail = lk.getOrderDetail().getOrder().getCustomer().getEmail();
        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(email)) {
            return VerificationResult.invalid("License key không thuộc về tài khoản " + email);
        }

        // ✅ Tất cả hợp lệ
        return VerificationResult.ok(expired);
    }

    /**
     * Phiên bản cũ: kiểm tra cơ bản (dành cho web)
     */
    public VerificationResult verifyPlainKey(String plainKey) {
        String hashed = HashUtil.sha256Hex(plainKey);
        Optional<LicenseKey> opt = repo.findByKey(hashed);
        if (opt.isEmpty()) {
            return VerificationResult.invalid("Key không đúng");
        }

        LicenseKey lk = opt.get();

        if (!"ACTIVATE".equalsIgnoreCase(lk.getStatus())) {
            return VerificationResult.invalid("Key chưa active hoặc không hợp lệ");
        }

        Date expired = lk.getExpiredAt();
        if (expired == null) {
            return VerificationResult.invalid("Key không có ngày hết hạn");
        }
        if (new Date().after(expired)) {
            return VerificationResult.invalid("Key đã hết hạn");
        }

        return VerificationResult.ok(expired);
    }

    public static class VerificationResult {
        private final boolean ok;
        private final String message;
        private final Date expiredAt;

        private VerificationResult(boolean ok, String message, Date expiredAt) {
            this.ok = ok;
            this.message = message;
            this.expiredAt = expiredAt;
        }

        public static VerificationResult ok(Date expiredAt) {
            return new VerificationResult(true, "OK", expiredAt);
        }

        public static VerificationResult invalid(String message) {
            return new VerificationResult(false, message, null);
        }

        public boolean isOk() { return ok; }
        public String getMessage() { return message; }
        public Date getExpiredAt() { return expiredAt; }
    }
}