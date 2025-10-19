package anbd.he191271.service;

import anbd.he191271.entity.ProductReviewReport;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Review;
import anbd.he191271.entity.Product;
import anbd.he191271.repository.ProductReviewReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductReviewReportService {

    @Autowired
    private ProductReviewReportRepository reportRepository;

    // Tạo report mới
    public ProductReviewReport createReport(Review review, Customer reporter, Product product,
                                            ProductReviewReport.ReportReason reason, String description) {

        // Kiểm tra không được report chính mình
        if (review.getCustomer().getId() == reporter.getId()) {
            throw new IllegalArgumentException("Bạn không thể report chính review của mình");
        }

        // Kiểm tra đã report chưa
        if (reportRepository.existsByReviewIdAndReporterId(review.getId(), (long) reporter.getId())) {
            throw new IllegalArgumentException("Bạn đã report review này rồi");
        }

        ProductReviewReport report = new ProductReviewReport(review, reporter, product, reason, description);
        return reportRepository.save(report);
    }

    // Lấy tất cả reports với phân trang
    public Page<ProductReviewReport> getAllReports(int page, int size) {
        return reportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    // Lấy report by ID
    public ProductReviewReport getReportById(Long id) {
        Optional<ProductReviewReport> report = reportRepository.findById(id);
        return report.orElse(null);
    }

    // Lấy reports theo status với phân trang
    public Page<ProductReviewReport> getReportsByStatus(ProductReviewReport.ReportStatus status, int page, int size) {
        return reportRepository.findByStatus(status, PageRequest.of(page, size));
    }

    // Lấy reports pending
    public List<ProductReviewReport> getPendingReports() {
        return reportRepository.findByStatus(ProductReviewReport.ReportStatus.PENDING);
    }

    // Lấy reports pending với phân trang
    public Page<ProductReviewReport> getPendingReportsPage(int page, int size) {
        return reportRepository.findByStatus(ProductReviewReport.ReportStatus.PENDING, PageRequest.of(page, size));
    }

    // Xử lý report (approve/reject)
    public ProductReviewReport resolveReport(Long reportId, ProductReviewReport.ReportStatus status, String adminNotes) {
        ProductReviewReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại"));

        report.setStatus(status);
        report.setAdminNotes(adminNotes);
        report.setResolvedAt(java.time.LocalDateTime.now());

        return reportRepository.save(report);
    }

    // Đếm số report theo status
    public long countReportsByStatus(ProductReviewReport.ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    // Kiểm tra user đã report review chưa
    public boolean hasUserReportedReview(Long reviewId, Long userId) {
        return reportRepository.existsByReviewIdAndReporterId(reviewId, userId);
    }

    // Lấy thống kê reports
    public java.util.Map<String, Object> getReportStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pending", countReportsByStatus(ProductReviewReport.ReportStatus.PENDING));
        stats.put("approved", countReportsByStatus(ProductReviewReport.ReportStatus.APPROVED));
        stats.put("rejected", countReportsByStatus(ProductReviewReport.ReportStatus.REJECTED));
        return stats;
    }
}