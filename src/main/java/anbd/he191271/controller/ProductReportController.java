package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.ProductReport;
import anbd.he191271.service.ProductReportService;
import anbd.he191271.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/history")
public class ProductReportController {


    private ProductService productService;
    private ProductReportService service;
    @Autowired
    public ProductReportController(ProductReportService service, ProductService productService) {
        this.service = service;
        this.productService = productService;
    }

    @GetMapping
    public String listReports(Model model) {
        List<ProductReport> reports = service.getAllReports();
        ProductReportService.ReportStatistics statistics = service.getReportStatistics();

        model.addAttribute("reports", reports);
        model.addAttribute("statistics", statistics);
        return "list";
    }

    // XEM CHI TIẾT BÁO CÁO
    @GetMapping("/view/{id}")
    public String viewReportDetail(@PathVariable Long id, Model model) {
        try {
            ProductReport report = service.getReportById(id);
            if (report != null) {
                model.addAttribute("report", report);
                // THÊM STATISTICS VÀO MODEL
                ProductReportService.ReportStatistics statistics = service.getReportStatistics();
                model.addAttribute("statistics", statistics);
                return "report-detail"; // Đảm bảo trả về đúng tên template
            } else {
                return "redirect:/history?error=Báo+cáo+không+tồn+tại";
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            return "redirect:/history?error=Lỗi+hệ+thống";
        }
    }

    // Chỉnh sửa báo cáo (chỉ cho pending)
    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        ProductReport report = service.getReportById(id);
        if (report != null && "pending".equals(report.getStatus())) {
            // Thêm danh sách sản phẩm
            List<Product> products = productService.getAllProduct();
            model.addAttribute("products", products);

            // Thêm danh sách các loại tiêu đề
            String[] reportTypes = {"Đề xuất", "Báo cáo", "Phản hồi", "Khiếu nại"};
            model.addAttribute("reportTypes", reportTypes);

            model.addAttribute("report", report);
            return "report-edit";
        } else {
            return "redirect:/history?error=Chỉ+có+thể+chỉnh+sửa+báo+cáo+đang+chờ+xử+lý";
        }
    }

    // Cập nhật báo cáo
    @PostMapping("/update/{id}")
    public String updateReport(@PathVariable Long id,
                               @ModelAttribute ProductReport updatedReport,
                               RedirectAttributes redirectAttributes) {
        try {
            ProductReport existingReport = service.getReportById(id);
            if (existingReport == null || !"pending".equals(existingReport.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Không thể chỉnh sửa báo cáo đã được xử lý");
                return "redirect:/history";
            }

            // Chỉ cập nhật các trường mà customer được phép sửa
            existingReport.setName(updatedReport.getName());
            existingReport.setEmail(updatedReport.getEmail());
            existingReport.setProductId(updatedReport.getProductId());
            existingReport.setTitle(updatedReport.getTitle());
            existingReport.setMessage(updatedReport.getMessage());
            existingReport.setDescription(updatedReport.getDescription());

            service.saveReport(existingReport);
            redirectAttributes.addFlashAttribute("success", "Cập nhật báo cáo thành công!");

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