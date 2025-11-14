package anbd.he191271.service;

import anbd.he191271.entity.Review;
import anbd.he191271.repository.LicenseKeyRepository;
import anbd.he191271.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private LicenseKeyRepository licenseKeyRepository; // ✅ THÊM DEPENDENCY NÀY

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại"));
    }

    // ✅ KIỂM TRA CUSTOMER ĐÃ MUA SẢN PHẨM CHƯA
    public boolean hasCustomerPurchasedProduct(int customerId, int productId) {
        try {
            // 🗃️ TRUY VẤN DATABASE KIỂM TRA
            long purchaseCount = reviewRepository.countByCustomerIdAndProductId(customerId, productId);

            // 📝 LOG ĐỂ DEBUG
            System.out.println("🔍 Purchase check - Customer: " + customerId +
                    ", Product: " + productId + ", Count: " + purchaseCount);

            return purchaseCount > 0; // → TRUE nếu đã mua ít nhất 1 license
        } catch (Exception e) {
            System.out.println("❌ Error checking purchase: " + e.getMessage());
            return false; // → FALSE nếu có lỗi
        }
    }

    public Page<Review> getFilteredReviews(int productId,
                                           Integer rating,
                                           Boolean hasImage,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           int page,
                                           int size,
                                           String sort) {

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        // Xác định cách sắp xếp
        Pageable pageable;
        if ("oldest".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        } else {
            // Mặc định sắp xếp theo mới nhất
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        }

        return reviewRepository.findFilteredReviews(
                productId, rating, hasImage, startDateTime, endDateTime, pageable);
    }

    public Map<String, Object> getReviewStats(int productId, Integer rating, Boolean hasImage,
                                              LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<Review> allReviews = reviewRepository.findFilteredReviews(
                productId, rating, hasImage, startDateTime, endDateTime, Pageable.unpaged());

        long totalReviews = allReviews.getTotalElements();
        double averageRating = 0.0;

        if (totalReviews > 0) {
            double totalRating = allReviews.getContent().stream()
                    .mapToInt(Review::getRating)
                    .sum();
            averageRating = totalRating / totalReviews;
        }

        stats.put("averageRating", String.format("%.1f", averageRating));
        stats.put("totalReviews", totalReviews);

        return stats;
    }
}