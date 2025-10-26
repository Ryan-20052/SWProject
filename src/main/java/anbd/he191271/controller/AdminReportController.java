package anbd.he191271.controller;

import anbd.he191271.dto.AdminReportDTO;
import anbd.he191271.entity.ProductReviewReport;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.ProductReviewReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "*") // Cho phép gọi từ frontend khác domain (tùy bạn cấu hình)
public class AdminReportController {

    @Autowired
    private ProductReviewReportService reportService;
    private final AdminLogService logService;

    public AdminReportController(AdminLogService logService) {
        this.logService = logService;
    }

    /**
     * Lấy danh sách tất cả báo cáo (phân trang)
     * GET /api/admin/reports?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var pageResult = reportService.getAllReportsDTO(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("reports", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("stats", reportService.getReportStats());

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết một report cụ thể
     * GET /api/admin/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        ProductReviewReport report = reportService.getReportById(id);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy báo cáo!"));
        }
        return ResponseEntity.ok(report);
    }

    /**
     * Duyệt (APPROVE) một báo cáo → ban người dùng vi phạm
     * POST /api/admin/reports/{id}/approve
     * Body: { "adminNotes": "..." }
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReport(@PathVariable Long id,
                                           @RequestBody(required = false) Map<String, String> body) {
        String adminNotes = body != null ? body.getOrDefault("adminNotes", null) : null;

        try {
            ProductReviewReport report = reportService.resolveReport(
                    id, ProductReviewReport.ReportStatus.APPROVED, adminNotes
            );
            logService.saveLog("Approved report of"+ report.getReporter().getUsername()+" about comment :"+report.getDescription(),"product_review_report");
            return ResponseEntity.ok(Map.of(
                    "message", "Đã duyệt báo cáo và ban người dùng vi phạm!",
                    "report", report
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Từ chối (REJECT) một báo cáo
     * POST /api/admin/reports/{id}/reject
     * Body: { "adminNotes": "..." }
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable Long id,
                                          @RequestBody(required = false) Map<String, String> body) {
        String adminNotes = body != null ? body.getOrDefault("adminNotes", null) : null;

        try {
            ProductReviewReport report = reportService.resolveReport(
                    id, ProductReviewReport.ReportStatus.REJECTED, adminNotes
            );
            logService.saveLog("Rejected report of"+ report.getReporter().getUsername()+" about comment :"+report.getDescription(),"product_review_report");

            return ResponseEntity.ok(Map.of(
                    "message", "Đã từ chối báo cáo.",
                    "report", report
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách các báo cáo đang chờ xử lý
     * GET /api/admin/reports/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ProductReviewReport>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getReportsByStatus(
            @PathVariable ProductReviewReport.ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var pageResult = reportService.getReportsByStatusDTO(status, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("reports", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("stats", reportService.getReportStats());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reporter,
            @RequestParam(required = false) String reported,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        Page<AdminReportDTO> result = reportService.searchReportsDTO(
                reporter, reported, reason, start, end, page, size, sortBy, order
        );

        Map<String, Object> response = new HashMap<>();
        response.put("reports", result.getContent());
        response.put("currentPage", result.getNumber());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return ResponseEntity.ok(response);
    }

}