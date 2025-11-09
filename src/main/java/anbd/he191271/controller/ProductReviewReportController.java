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