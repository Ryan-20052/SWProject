package anbd.he191271.service;

import anbd.he191271.entity.Review;
import anbd.he191271.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại"));
    }

    public Page<Review> getFilteredReviews(int productId,
                                           Integer rating,
                                           Boolean hasImage,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           int page,
                                           int size) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        return reviewRepository.findFilteredReviews(
                productId, rating, hasImage, startDateTime, endDateTime, PageRequest.of(page, size));
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