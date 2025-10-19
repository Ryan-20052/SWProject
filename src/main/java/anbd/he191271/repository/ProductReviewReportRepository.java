package anbd.he191271.repository;

import anbd.he191271.entity.ProductReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewReportRepository extends JpaRepository<ProductReviewReport, Long> {

    // Tìm reports theo status
    List<ProductReviewReport> findByStatus(ProductReviewReport.ReportStatus status);

    // Tìm reports pending với phân trang
    Page<ProductReviewReport> findByStatus(ProductReviewReport.ReportStatus status, Pageable pageable);

    // Tìm reports của 1 review cụ thể
    List<ProductReviewReport> findByReviewId(Long reviewId);

    // Tìm reports của 1 user (người report)
    List<ProductReviewReport> findByReporterId(Long reporterId);

    // Kiểm tra user đã report review này chưa
    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);

    // Đếm số report theo status
    long countByStatus(ProductReviewReport.ReportStatus status);

    // Tìm reports của 1 product
    @Query("SELECT r FROM ProductReviewReport r WHERE r.product.id = :productId")
    List<ProductReviewReport> findByProductId(@Param("productId") Long productId);

    // Tìm tất cả reports với phân trang và sắp xếp
    Page<ProductReviewReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}