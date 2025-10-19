package anbd.he191271.service;

import anbd.he191271.entity.Review;
import anbd.he191271.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại"));
    }
    public Page<Review> getFilteredReviews(int productId,
                                           Integer rating,
                                           Boolean hasImage,
                                           Date startDate,
                                           Date endDate,
                                           int page,
                                           int size) {
        return reviewRepository.findFilteredReviews(productId, rating, hasImage, startDate, endDate, PageRequest.of(page, size));
    }

    // Thêm method tính toán thống kê
    public Map<String, Object> getReviewStats(int productId, Integer rating, Boolean hasImage, Date startDate, Date endDate) {
        Map<String, Object> stats = new HashMap<>();

        // Lấy tất cả reviews (không phân trang) để tính toán
        Page<Review> allReviews = reviewRepository.findFilteredReviews(productId, rating, hasImage, startDate, endDate, Pageable.unpaged());

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
