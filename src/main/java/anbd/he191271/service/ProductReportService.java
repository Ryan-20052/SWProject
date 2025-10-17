package anbd.he191271.service;

import anbd.he191271.entity.ProductReport;
import anbd.he191271.repository.ProductReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductReportService {

    @Autowired
    private ProductReportRepository productReportRepository;

    // Lấy tất cả báo cáo
    public List<ProductReport> getAllReports() {
        return productReportRepository.findAll();
    }

    // Lấy báo cáo theo ID
    public ProductReport getReportById(Long id) {
        return productReportRepository.findById(id).orElse(null);
    }

    // Lấy báo cáo đang chờ xử lý
    public List<ProductReport> getPendingReports() {
        return productReportRepository.findByStatus("pending");
    }

    // Lấy báo cáo đã được phê duyệt
    public List<ProductReport> getApprovedReports() {
        return productReportRepository.findByStatus("approved");
    }

    // Lấy báo cáo đã bị từ chối
    public List<ProductReport> getRejectedReports() {
        return productReportRepository.findByStatus("rejected");
    }

    // Lưu báo cáo
    public ProductReport saveReport(ProductReport report) {
        return productReportRepository.save(report);
    }

    // Cập nhật trạng thái báo cáo
    public void updateReportStatus(Long id, String status) {
        ProductReport report = getReportById(id);
        if (report != null) {
            report.setStatus(status);
            productReportRepository.save(report);
        }
    }

    // XÓA BÁO CÁO - CHỈ CHO PHÉP XÓA KHI STATUS LÀ "pending"
    public boolean deleteReport(Long id) {
        ProductReport report = getReportById(id);
        if (report != null && "pending".equals(report.getStatus())) {
            productReportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // XÓA NHIỀU BÁO CÁO - CHỈ XÓA NHỮNG BÁO CÁO CÓ STATUS LÀ "pending"
    public DeleteResult deleteMultipleReports(List<Long> ids) {
        int deletedCount = 0;
        int skippedCount = 0;

        for (Long id : ids) {
            ProductReport report = getReportById(id);
            if (report != null && "pending".equals(report.getStatus())) {
                productReportRepository.deleteById(id);
                deletedCount++;
            } else {
                skippedCount++;
            }
        }

        return new DeleteResult(deletedCount, skippedCount);
    }

    // Lấy báo cáo theo trạng thái
    public List<ProductReport> getReportsByStatus(String status) {
        return productReportRepository.findByStatus(status);
    }

    // Thống kê tổng quan
    public ReportStatistics getReportStatistics() {
        List<ProductReport> allReports = getAllReports();
        long total = allReports.size();
        long pending = getPendingReports().size();
        long approved = getApprovedReports().size();
        long rejected = getRejectedReports().size();

        return new ReportStatistics(total, pending, approved, rejected);
    }

    // Class để trả về kết quả xóa
    public static class DeleteResult {
        private final int deletedCount;
        private final int skippedCount;

        public DeleteResult(int deletedCount, int skippedCount) {
            this.deletedCount = deletedCount;
            this.skippedCount = skippedCount;
        }

        public int getDeletedCount() {
            return deletedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }
    }

    // Class để trả về thống kê
    public static class ReportStatistics {
        private final long total;
        private final long pending;
        private final long approved;
        private final long rejected;

        public ReportStatistics(long total, long pending, long approved, long rejected) {
            this.total = total;
            this.pending = pending;
            this.approved = approved;
            this.rejected = rejected;
        }

        public long getTotal() { return total; }
        public long getPending() { return pending; }
        public long getApproved() { return approved; }
        public long getRejected() { return rejected; }
    }
}