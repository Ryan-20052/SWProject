package anbd.he191271.repository;

import anbd.he191271.dto.AdminReportDTO;
import anbd.he191271.entity.ProductReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductReviewReportRepository extends org.springframework.data.jpa.repository.JpaRepository<ProductReviewReport, Long> {

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
    @Query("""
    SELECT new anbd.he191271.dto.AdminReportDTO(
        r.id,
        reporter.username,
        COALESCE(rvCustomer.username, 'N/A'),
        CAST(r.reportReason AS string),
        r.description,
        CAST(r.status AS string),
        r.createdAt
    )
    FROM ProductReviewReport r
    LEFT JOIN r.reporter reporter
    LEFT JOIN r.review review
    LEFT JOIN review.customer rvCustomer
    WHERE (:reporter IS NULL OR LOWER(reporter.username) LIKE LOWER(CONCAT('%', :reporter, '%')))
      AND (:reported IS NULL OR LOWER(rvCustomer.username) LIKE LOWER(CONCAT('%', :reported, '%')))
      AND (:reason IS NULL OR LOWER(CAST(r.reportReason AS string)) LIKE LOWER(CONCAT('%', :reason, '%')))
      AND (:start IS NULL OR r.createdAt >= :start)
      AND (:end IS NULL OR r.createdAt < :end)
    """)
    Page<AdminReportDTO> searchReportsDTO(
            @Param("reporter") String reporter,
            @Param("reported") String reported,
            @Param("reason") String reason,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}