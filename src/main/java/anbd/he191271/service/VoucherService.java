package anbd.he191271.service;

import anbd.he191271.entity.Voucher;
import anbd.he191271.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    private static final double MINIMUM_AMOUNT = 5000.0;

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
        validateVoucher(voucher);
        voucher.setCode(voucher.getCode().trim().toUpperCase());
        voucher.setUsedCount(0);
        return voucherRepository.save(voucher);
    }

    public Voucher updateVoucher(Long id, Voucher updated) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        validateVoucher(updated);

        v.setDiscountValue(updated.getDiscountValue());
        v.setPercent(updated.isPercent());
        v.setUsageLimit(updated.getUsageLimit());
        v.setStartDate(updated.getStartDate());
        v.setEndDate(updated.getEndDate());
        v.setActive(updated.isActive());
        v.setMaxDiscountAmount(updated.getMaxDiscountAmount()); // 🔥 Cập nhật max discount
        v.setMinOrderAmount(updated.getMinOrderAmount()); // 🔥 Cập nhật min order amount

        return voucherRepository.save(v);
    }

    @Transactional
    public void deactivateVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setActive(false);
        voucherRepository.save(v);
        System.out.println("🚫 Voucher " + v.getCode() + " đã bị khóa, không thể sử dụng nữa.");
    }

    @Transactional
    public void activateVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setActive(true);
        voucherRepository.save(v);
        System.out.println("✅ Voucher " + v.getCode() + " đã được mở khóa.");
    }

    // ========== CUSTOMER FUNCTIONS ==========

    /**
     * ✅ Áp dụng voucher để tính thử giảm giá (KHÔNG trừ lượt sử dụng)
     */
    public double applyVoucher(String code, double orderTotal) {
        Voucher v = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại!"));

        if (!isVoucherUsable(v, orderTotal)) {
            throw new RuntimeException("Voucher không thể áp dụng cho đơn hàng này!");
        }

        double discountAmount = calculateDiscountAmount(v, orderTotal);
        double discountedTotal = orderTotal - discountAmount;

        // 🔥 Đảm bảo không dưới 5,000đ
        if (discountedTotal < MINIMUM_AMOUNT) {
            discountedTotal = MINIMUM_AMOUNT;
        }

        return discountedTotal;
    }

    /**
     * ✅ Tính toán số tiền được giảm dựa trên voucher
     */
    private double calculateDiscountAmount(Voucher v, double orderTotal) {
        double discountAmount;

        if (v.isPercent()) {
            // Tính giảm theo %
            discountAmount = orderTotal * (v.getDiscountValue() / 100);

            // Áp dụng mức giảm tối đa nếu có
            if (v.getMaxDiscountAmount() != null && discountAmount > v.getMaxDiscountAmount()) {
                discountAmount = v.getMaxDiscountAmount();
            }
        } else {
            // Giảm theo số tiền cố định
            discountAmount = v.getDiscountValue();
        }

        return discountAmount;
    }

    /**
     * ✅ Kiểm tra tính hợp lệ của voucher
     */
    private boolean isVoucherUsable(Voucher v, double orderTotal) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra active
        if (!v.isActive()) {
            throw new RuntimeException("Voucher đã bị khóa!");
        }

        // 2. Kiểm tra ngày hiệu lực
        if (v.getStartDate() != null && now.isBefore(v.getStartDate())) {
            throw new RuntimeException("Voucher chưa đến thời gian sử dụng!");
        }

        if (v.getEndDate() != null && now.isAfter(v.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn!");
        }

        // 3. Kiểm tra lượt sử dụng
        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng!");
        }

        // 🔥 4. Kiểm tra số tiền đơn hàng tối thiểu
        if (orderTotal < v.getMinOrderAmount()) {
            throw new RuntimeException("Đơn hàng phải có giá trị tối thiểu " +
                    String.format("%,.0f", v.getMinOrderAmount()) + "đ để áp dụng voucher!");
        }

        return true;
    }
    private boolean isVoucherUsableBasic(Voucher v) {
        LocalDateTime now = LocalDateTime.now();

        boolean withinDate = (v.getStartDate() == null || !now.isBefore(v.getStartDate())) &&
                (v.getEndDate() == null || !now.isAfter(v.getEndDate()));
        boolean active = v.isActive();

        return withinDate && active;
    }


    /**
     * ✅ Giảm 1 lượt sử dụng sau khi thanh toán thành công
     */
    @Transactional
    public void decreaseUsage(String code) { // 🔥 XÓA orderTotal parameter
        Voucher v = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + code));

        // 🔥 CHỈ kiểm tra điều kiện cơ bản, KHÔNG kiểm tra minOrderAmount lại
        if (!isVoucherUsableBasic(v)) {
            throw new RuntimeException("Voucher không thể sử dụng!");
        }

        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng!");
        }

        v.setUsedCount(v.getUsedCount() + 1);
        voucherRepository.save(v);

        System.out.println("✅ Voucher " + v.getCode() + " đã được dùng " +
                v.getUsedCount() + "/" + v.getUsageLimit() + " lần.");
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

    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }

    private void validateVoucher(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra ngày bắt đầu không được trong quá khứ
        if (voucher.getStartDate() != null && voucher.getStartDate().isBefore(now)) {
            throw new RuntimeException("Ngày bắt đầu không được trong quá khứ!");
        }

        // 2. Kiểm tra ngày kết thúc phải sau ngày bắt đầu
        if (voucher.getStartDate() != null && voucher.getEndDate() != null
                && voucher.getEndDate().isBefore(voucher.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }

        // 3. Kiểm tra voucher % không vượt quá 100%
        if (voucher.isPercent() && voucher.getDiscountValue() > 100) {
            throw new RuntimeException("Voucher giảm giá % không được vượt quá 100%!");
        }

        // 4. Kiểm tra giá trị voucher phải lớn hơn 0
        if (voucher.getDiscountValue() <= 0) {
            throw new RuntimeException("Giá trị voucher phải lớn hơn 0!");
        }

        // 5. Kiểm tra mã voucher không được trống
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            throw new RuntimeException("Mã voucher không được để trống!");
        }

        // 6. Kiểm tra giới hạn sử dụng phải lớn hơn 0 nếu có
        if (voucher.getUsageLimit() != null && voucher.getUsageLimit() <= 0) {
            throw new RuntimeException("Giới hạn sử dụng phải lớn hơn 0!");
        }

        // 🔥 7. Kiểm tra mức giảm tối đa (chỉ áp dụng cho voucher %)
        if (voucher.isPercent() && voucher.getMaxDiscountAmount() != null
                && voucher.getMaxDiscountAmount() <= 0) {
            throw new RuntimeException("Mức giảm tối đa phải lớn hơn 0!");
        }

        // 🔥 8. Kiểm tra số tiền đơn hàng tối thiểu
        if (voucher.getMinOrderAmount() == null || voucher.getMinOrderAmount() < 0) {
            throw new RuntimeException("Số tiền đơn hàng tối thiểu không hợp lệ!");
        }
    }
}