package anbd.he191271.controller;

import anbd.he191271.entity.ProductReviewReport;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Review;
import anbd.he191271.service.ProductReviewReportService;
import anbd.he191271.service.ReviewService;
import anbd.he191271.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/report/review")
public class ProductReviewReportController {

    @Autowired
    private ProductReviewReportService reportService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    // ====== CUSTOMER SIDE ======

    // Hiển thị form báo cáo (trang riêng) - ĐÃ SỬA
    @GetMapping("/form/{reviewId}")
    public String showReportForm(@PathVariable Long reviewId,
                                 Model model,
                                 HttpSession session,
                                 HttpServletRequest request) {

        // Kiểm tra đăng nhập
        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            String currentUrl = request.getRequestURL().toString();
            return "redirect:/login.html?redirect=" + currentUrl;
        }

        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            model.addAttribute("error", "Đánh giá không tồn tại");
            return "redirect:/home/homepage";
        }

        // Kiểm tra không được report chính mình - ĐÃ SỬA
        if (review.getCustomer().getId() == currentCustomer.getId()) {
            model.addAttribute("error", "Bạn không thể báo cáo chính đánh giá của mình");
            return "redirect:/review/list?productId=" + review.getProduct().getId();
        }

        // Kiểm tra đã report chưa - ĐÃ SỬA
        if (reportService.hasUserReportedReview(reviewId, (long) currentCustomer.getId())) {
            model.addAttribute("error", "Bạn đã báo cáo đánh giá này rồi");
            return "redirect:/review/list?productId=" + review.getProduct().getId();
        }

        model.addAttribute("review", review);
        model.addAttribute("isEdit", false);
        return "reviewReportForm";
    }

    // Xử lý submit form báo cáo (POST) - ĐÃ SỬA
    @PostMapping("/submit")
    public String submitReport(@RequestParam Long reviewId,
                               @RequestParam String reportReason,
                               @RequestParam(required = false) String description,
                               HttpSession session,
                               Model model) {

        // Kiểm tra đăng nhập
        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            return "redirect:/customer/login";
        }

        Review review = null;
        try {
            review = reviewService.getReviewById(reviewId);
            if (review == null) {
                model.addAttribute("error", "Đánh giá không tồn tại");
                return "reviewReportForm";
            }

            // Kiểm tra trong POST - ĐÃ SỬA
            if (review.getCustomer().getId() == currentCustomer.getId()) {
                model.addAttribute("error", "Bạn không thể báo cáo chính đánh giá của mình");
                model.addAttribute("review", review);
                model.addAttribute("isEdit", false);
                return "reviewReportForm";
            }

            // ĐÃ SỬA
            if (reportService.hasUserReportedReview(reviewId, (long) currentCustomer.getId())) {
                model.addAttribute("error", "Bạn đã báo cáo đánh giá này rồi");
                model.addAttribute("review", review);
                model.addAttribute("isEdit", false);
                return "reviewReportForm";
            }

            ProductReviewReport.ReportReason reason = ProductReviewReport.ReportReason.valueOf(reportReason.toUpperCase());

            ProductReviewReport report = reportService.createReport(
                    review, currentCustomer, review.getProduct(), reason, description);

            model.addAttribute("success", true);
            model.addAttribute("message", "Báo cáo của bạn đã được gửi thành công!");
            model.addAttribute("review", review);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Lý do báo cáo không hợp lệ: " + e.getMessage());
            if (review == null) {
                review = reviewService.getReviewById(reviewId);
            }
            model.addAttribute("review", review);
            model.addAttribute("isEdit", false);
            return "reviewReportForm";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi gửi báo cáo: " + e.getMessage());
            if (review == null) {
                review = reviewService.getReviewById(reviewId);
            }
            model.addAttribute("review", review);
            model.addAttribute("isEdit", false);
            return "reviewReportForm";
        }

        return "reportSuccess";
    }

    // API tạo report (AJAX) - ĐÃ SỬA
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createReport(
            @RequestParam Long reviewId,
            @RequestParam String reportReason,
            @RequestParam(required = false) String description,
            HttpSession session) {

        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Vui lòng đăng nhập để báo cáo"));
        }

        try {
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Đánh giá không tồn tại"));
            }

            // Kiểm tra - ĐÃ SỬA
            if (review.getCustomer().getId() == currentCustomer.getId()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Bạn không thể báo cáo chính đánh giá của mình"));
            }

            // ĐÃ SỬA
            if (reportService.hasUserReportedReview(reviewId, (long) currentCustomer.getId())) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Bạn đã báo cáo đánh giá này rồi"));
            }

            ProductReviewReport.ReportReason reason = ProductReviewReport.ReportReason.valueOf(reportReason.toUpperCase());

            ProductReviewReport report = reportService.createReport(
                    review, currentCustomer, review.getProduct(), reason, description);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Báo cáo đã được gửi thành công");
            response.put("reportId", report.getId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Lý do báo cáo không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", "Có lỗi xảy ra khi gửi báo cáo"));
        }
    }

    // API kiểm tra user đã report review chưa - ĐÃ SỬA
    @GetMapping("/check/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> checkUserReported(
            @PathVariable Long reviewId,
            HttpSession session) {

        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            return ResponseEntity.ok(Map.of("hasReported", false, "isLoggedIn", false));
        }

        // ĐÃ SỬA
        boolean hasReported = reportService.hasUserReportedReview(reviewId, (long) currentCustomer.getId());
        return ResponseEntity.ok(Map.of("hasReported", hasReported, "isLoggedIn", true));
    }

    // ====== ADMIN SIDE ======

    // Trang quản lý reports
    @GetMapping("/admin")
    public String adminReportManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {

        Page<ProductReviewReport> reportsPage;

        if (status != null && !status.isEmpty()) {
            try {
                ProductReviewReport.ReportStatus reportStatus = ProductReviewReport.ReportStatus.valueOf(status.toUpperCase());
                reportsPage = reportService.getReportsByStatus(reportStatus, page, size);
            } catch (IllegalArgumentException e) {
                reportsPage = reportService.getAllReports(page, size);
            }
        } else {
            reportsPage = reportService.getAllReports(page, size);
        }

        Map<String, Object> stats = reportService.getReportStats();

        model.addAttribute("reports", reportsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportsPage.getTotalPages());
        model.addAttribute("totalItems", reportsPage.getTotalElements());
        model.addAttribute("stats", stats);
        model.addAttribute("statusFilter", status);

        return "admin-review-reports";
    }

    // API xử lý report (approve/reject)
    @PostMapping("/admin/resolve/{reportId}")
    @ResponseBody
    public ResponseEntity<?> resolveReport(
            @PathVariable Long reportId,
            @RequestParam String action,
            @RequestParam(required = false) String adminNotes) {

        try {
            ProductReviewReport.ReportStatus status = "approve".equals(action)
                    ? ProductReviewReport.ReportStatus.APPROVED
                    : ProductReviewReport.ReportStatus.REJECTED;

            ProductReviewReport report = reportService.resolveReport(reportId, status, adminNotes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xử lý report thành công");
            response.put("report", report);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Chi tiết report
    @GetMapping("/admin/detail/{reportId}")
    public String reportDetail(@PathVariable Long reportId, Model model) {
        ProductReviewReport report = reportService.getReportById(reportId);
        if (report == null) {
            return "redirect:/report/review/admin";
        }

        model.addAttribute("report", report);
        return "report-detail";
    }

    // Form chỉnh sửa report - ĐÃ SỬA
    @GetMapping("/edit/{reportId}")
    public String editReportForm(@PathVariable Long reportId,
                                 Model model,
                                 HttpSession session,
                                 HttpServletRequest request) {

        // Kiểm tra đăng nhập
        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            String currentUrl = request.getRequestURL().toString();
            return "redirect:/login.html?redirect=" + currentUrl;
        }

        // Lấy report theo ID
        ProductReviewReport report = reportService.getReportById(reportId);

        // Kiểm tra report tồn tại và thuộc về user hiện tại - ĐÃ SỬA
        if (report == null || report.getReporter().getId() != currentCustomer.getId()) {
            model.addAttribute("error", "Báo cáo không tồn tại hoặc bạn không có quyền chỉnh sửa");
            return "redirect:/home/my-reported-reviews";
        }

        // Kiểm tra chỉ cho phép chỉnh sửa khi status là PENDING
        if (report.getStatus() != ProductReviewReport.ReportStatus.PENDING) {
            model.addAttribute("error", "Chỉ có thể chỉnh sửa báo cáo đang chờ xử lý");
            return "redirect:/home/my-reported-reviews";
        }

        model.addAttribute("report", report);
        model.addAttribute("review", report.getReview());
        model.addAttribute("isEdit", true);
        return "reviewReportForm";
    }

    // Xử lý cập nhật report - ĐÃ SỬA
    @PostMapping("/update/{reportId}")
    public String updateReport(@PathVariable Long reportId,
                               @RequestParam String reportReason,
                               @RequestParam(required = false) String description,
                               HttpSession session,
                               Model model) {

        // Kiểm tra đăng nhập
        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            return "redirect:/login.html";
        }

        try {
            // Lấy report hiện tại
            ProductReviewReport existingReport = reportService.getReportById(reportId);

            // Kiểm tra quyền - ĐÃ SỬA
            if (existingReport == null || existingReport.getReporter().getId() != currentCustomer.getId()) {
                model.addAttribute("error", "Báo cáo không tồn tại hoặc bạn không có quyền chỉnh sửa");
                return "redirect:/home/my-reported-reviews";
            }

            // Kiểm tra status
            if (existingReport.getStatus() != ProductReviewReport.ReportStatus.PENDING) {
                model.addAttribute("error", "Chỉ có thể chỉnh sửa báo cáo đang chờ xử lý");
                return "redirect:/home/my-reported-reviews";
            }

            ProductReviewReport.ReportReason reason = ProductReviewReport.ReportReason.valueOf(reportReason.toUpperCase());

            // Cập nhật report
            ProductReviewReport updatedReport = reportService.updateReport(reportId, reason, description);

            model.addAttribute("success", true);
            model.addAttribute("message", "Cập nhật báo cáo thành công!");
            model.addAttribute("review", updatedReport.getReview());
            model.addAttribute("report", updatedReport);

            return "reportSuccess";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Lý do báo cáo không hợp lệ: " + e.getMessage());

            ProductReviewReport report = reportService.getReportById(reportId);
            model.addAttribute("report", report);
            model.addAttribute("review", report.getReview());
            model.addAttribute("isEdit", true);
            return "reviewReportForm";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật báo cáo: " + e.getMessage());

            ProductReviewReport report = reportService.getReportById(reportId);
            model.addAttribute("report", report);
            model.addAttribute("review", report.getReview());
            model.addAttribute("isEdit", true);
            return "reviewReportForm";
        }
    }
}