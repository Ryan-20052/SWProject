package anbd.he191271.service;

import anbd.he191271.entity.Voucher;
import anbd.he191271.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    // ========== MANAGER FUNCTIONS ==========

    @Transactional
    public Map<String, Object> getAllVouchersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> voucherPage = voucherRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("vouchers", voucherPage.getContent());
        result.put("currentPage", voucherPage.getNumber());
        result.put("totalItems", voucherPage.getTotalElements());
        result.put("totalPages", voucherPage.getTotalPages());
        return result;
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

    @Transactional
    public void deactivateVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setActive(false); // 🔒 Khóa voucher
        voucherRepository.save(v);

        System.out.println("🚫 Voucher " + v.getCode() + " đã bị khóa, không thể sử dụng nữa.");
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
    public List<Voucher> searchVouchers(String code, Boolean percent, Boolean active) {
        List<Voucher> all = voucherRepository.findAll();

        return all.stream()
                .filter(v -> code == null || v.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(v -> percent == null || v.isPercent() == percent)
                .filter(v -> active == null || v.isActive() == active)
                .sorted(Comparator.comparingLong(Voucher::getId))
                .toList();
    }

    @Transactional
    public void activateVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setActive(true); // 🔓 Mở khóa voucher
        voucherRepository.save(v);

        System.out.println("✅ Voucher " + v.getCode() + " đã được mở khóa.");
    }
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }
}