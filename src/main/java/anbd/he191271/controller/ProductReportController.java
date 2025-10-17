package anbd.he191271.controller;

import anbd.he191271.entity.ProductReport;
import anbd.he191271.service.ProductReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/history")
public class ProductReportController {

    @Autowired
    private ProductReportService service;

    @GetMapping
    public String listReports(Model model) {
        List<ProductReport> reports = service.getAllReports();
        ProductReportService.ReportStatistics statistics = service.getReportStatistics();

        model.addAttribute("reports", reports);
        model.addAttribute("statistics", statistics);
        return "list";
    }

    // Xem chi tiết báo cáo
    @GetMapping("/view/{id}")
    public String viewReportDetail(@PathVariable Long id, Model model) {
        ProductReport report = service.getReportById(id);
        if (report != null) {
            model.addAttribute("report", report);
            return "report-detail";
        } else {
            return "redirect:/history?error=Report+not+found";
        }
    }

    // Chỉnh sửa báo cáo (chuyển trạng thái) - CHỈ CHO PENDING
    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        ProductReport report = service.getReportById(id);
        if (report != null && "pending".equals(report.getStatus())) {
            model.addAttribute("report", report);
            return "report-edit";
        } else {
            return "redirect:/history?error=Chỉ+có+thể+chỉnh+sửa+báo+cáo+đang+chờ+xử+lý";
        }
    }

    // Cập nhật báo cáo - CÓ THỂ CHUYỂN THÀNH approved HOẶC rejected
    @PostMapping("/update/{id}")
    public String updateReport(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate status
            if (!"approved".equals(status) && !"rejected".equals(status) && !"pending".equals(status)) {
                redirectAttributes.addFlashAttribute("error", "Trạng thái không hợp lệ");
                return "redirect:/history/edit/" + id;
            }

            service.updateReportStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái báo cáo thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật báo cáo: " + e.getMessage());
        }
        return "redirect:/history";
    }

    // XÓA BÁO CÁO - CHỈ CHO PHÉP XÓA KHI STATUS LÀ "pending"
    @PostMapping("/delete/{id}")
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = service.deleteReport(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Xóa báo cáo thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể xóa báo cáo. Chỉ có thể xóa các báo cáo đang chờ xử lý (pending).");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa báo cáo: " + e.getMessage());
        }
        return "redirect:/history";
    }

    // XÓA NHIỀU BÁO CÁO - CHỈ XÓA NHỮNG BÁO CÁO CÓ STATUS LÀ "pending"
    @PostMapping("/delete-multiple")
    public String deleteMultipleReports(@RequestParam List<Long> reportIds,
                                        RedirectAttributes redirectAttributes) {
        try {
            ProductReportService.DeleteResult result = service.deleteMultipleReports(reportIds);

            if (result.getDeletedCount() > 0) {
                String message = "Đã xóa " + result.getDeletedCount() + " báo cáo đang chờ xử lý thành công!";
                if (result.getSkippedCount() > 0) {
                    message += " (" + result.getSkippedCount() + " báo cáo đã được xử lý không thể xóa)";
                }
                redirectAttributes.addFlashAttribute("success", message);
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Không có báo cáo nào được xóa. Chỉ có thể xóa các báo cáo đang chờ xử lý (pending).");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa báo cáo: " + e.getMessage());
        }
        return "redirect:/history";
    }

    // Lọc báo cáo theo trạng thái
    @GetMapping("/filter")
    public String filterReports(@RequestParam String status, Model model) {
        List<ProductReport> filteredReports;
        ProductReportService.ReportStatistics statistics = service.getReportStatistics();

        if ("all".equals(status)) {
            filteredReports = service.getAllReports();
        } else {
            filteredReports = service.getReportsByStatus(status);
        }

        model.addAttribute("reports", filteredReports);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentFilter", status);
        return "list";
    }
}