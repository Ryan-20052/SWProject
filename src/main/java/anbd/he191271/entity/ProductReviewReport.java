package anbd.he191271.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_review_reports")
public class ProductReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private Customer reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason", nullable = false)
    private ReportReason reportReason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ====== ENUMS ======
    public enum ReportReason {
        SPAM("spam"),
        INAPPROPRIATE("inappropriate"),
        HARASSMENT("harassment"),
        FALSE_INFORMATION("false_information"),
        HATE_SPEECH("hate_speech"),
        OTHER("other");

        private final String value;

        ReportReason(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ReportStatus {
        PENDING("pending"),
        APPROVED("approved"),
        REJECTED("rejected");

        private final String value;

        ReportStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // ====== CONSTRUCTORS ======
    public ProductReviewReport() {}

    public ProductReviewReport(Review review, Customer reporter, Product product,
                               ReportReason reportReason, String description) {
        this.review = review;
        this.reporter = reporter;
        this.product = product;
        this.reportReason = reportReason;
        this.description = description;
    }

    // ====== PRE-UPDATE HOOK ======
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ====== BUSINESS METHODS ======
    public boolean isResolved() {
        return this.resolvedAt != null;
    }

    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == ReportStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == ReportStatus.REJECTED;
    }

    // ====== GETTERS & SETTERS ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }

    public Customer getReporter() { return reporter; }
    public void setReporter(Customer reporter) { this.reporter = reporter; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public ReportReason getReportReason() { return reportReason; }
    public void setReportReason(ReportReason reportReason) { this.reportReason = reportReason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ====== TO STRING ======
    @Override
    public String toString() {
        return "ProductReviewReport{" +
                "id=" + id +
                ", reviewId=" + (review != null ? review.getId() : "null") +
                ", reporterId=" + (reporter != null ? reporter.getId() : "null") +
                ", productId=" + (product != null ? product.getId() : "null") +
                ", reportReason=" + reportReason +
                ", status=" + status +
                '}';
    }
}