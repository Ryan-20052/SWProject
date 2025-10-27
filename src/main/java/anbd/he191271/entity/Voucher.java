package anbd.he191271.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Mã voucher (VD: SALE10, FREESHIP)

    @Column(nullable = false)
    private Double discountValue; // Giá trị giảm (theo % hoặc VNĐ)

    @Column(nullable = false)
    private boolean percent; // true = giảm theo %, false = giảm theo số tiền

    @Column(nullable = false)
    private Integer usageLimit; // Số lần được sử dụng

    @Column(nullable = false)
    private Integer usedCount = 0; // Đã dùng bao nhiêu lần

    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu

    @Column(nullable = false)
    private LocalDateTime endDate; // Ngày hết hạn

    @Column(nullable = false)
    private boolean active = true; // Đang hoạt động?

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isPercent() {
        return percent;
    }

    public void setPercent(boolean percent) {
        this.percent = percent;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValidNow() {
        LocalDateTime now = LocalDateTime.now();
        return active && usedCount < usageLimit && now.isAfter(startDate) && now.isBefore(endDate);
    }
}
