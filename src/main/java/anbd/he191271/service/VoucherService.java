package anbd.he191271.service;

import anbd.he191271.entity.Voucher;
import anbd.he191271.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public double applyVoucher(String code, double orderTotal) {
        Voucher v = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại!"));

        if (!v.isValidNow()) {
            throw new RuntimeException("Voucher đã hết hạn hoặc không còn hiệu lực!");
        }

        double discountAmount = v.isPercent()
                ? orderTotal * (v.getDiscountValue() / 100)
                : v.getDiscountValue();

        v.setUsedCount(v.getUsedCount() + 1);
        voucherRepository.save(v);

        return Math.max(0, orderTotal - discountAmount);
    }

    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }
}