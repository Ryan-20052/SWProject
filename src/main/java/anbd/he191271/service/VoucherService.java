package anbd.he191271.service;

import anbd.he191271.entity.Voucher;
import anbd.he191271.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    // ========== MANAGER FUNCTIONS ==========

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher createVoucher(Voucher voucher) {
        voucher.setCode(voucher.getCode().trim().toUpperCase());
        voucher.setUsedCount(0);
        return voucherRepository.save(voucher);
    }

    public Voucher updateVoucher(Long id, Voucher updated) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        v.setDiscountValue(updated.getDiscountValue());
        v.setPercent(updated.isPercent());
        v.setUsageLimit(updated.getUsageLimit());
        v.setStartDate(updated.getStartDate());
        v.setEndDate(updated.getEndDate());
        v.setActive(updated.isActive());

        return voucherRepository.save(v);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    // ========== CUSTOMER FUNCTIONS ==========

    /**
     * ✅ Áp dụng voucher để tính thử giảm giá (KHÔNG trừ lượt sử dụng)
     */
    public double applyVoucher(String code, double orderTotal) {
        Voucher v = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại!"));

        if (!isVoucherUsable(v)) {
            throw new RuntimeException("Voucher đã hết hạn hoặc không còn hiệu lực!");
        }

        double discountAmount = v.isPercent()
                ? orderTotal * (v.getDiscountValue() / 100)
                : v.getDiscountValue();

        return Math.max(0, orderTotal - discountAmount);
    }

    /**
     * ✅ Giảm 1 lượt sử dụng sau khi thanh toán thành công
     */
    @Transactional
    public void decreaseUsage(String code) {
        Voucher v = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + code));

        if (!isVoucherUsable(v)) {
            throw new RuntimeException("Voucher đã hết hạn hoặc không còn hiệu lực!");
        }

        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng!");
        }

        v.setUsedCount(v.getUsedCount() + 1);
        voucherRepository.save(v);

        System.out.println("✅ Voucher " + v.getCode() + " đã được dùng " +
                v.getUsedCount() + "/" + v.getUsageLimit() + " lần.");
    }

    /**
     * ✅ Kiểm tra tính hợp lệ của voucher (active, ngày, lượt dùng)
     */
    private boolean isVoucherUsable(Voucher v) {
        LocalDateTime now = LocalDateTime.now();

        boolean withinDate = (v.getStartDate() == null || !now.isBefore(v.getStartDate())) &&
                (v.getEndDate() == null || !now.isAfter(v.getEndDate()));
        boolean withinUsage = (v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit());
        boolean active = v.isActive();

        return withinDate && withinUsage && active;
    }

    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }
}