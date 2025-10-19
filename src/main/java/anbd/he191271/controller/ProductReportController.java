package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.ProductReport;
import anbd.he191271.service.ProductReportService;
import anbd.he191271.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Map để chuyển đổi giữa display value và database value
    private final Map<String, String> titleMapping = new HashMap<String, String>() {{
        put("Đề xuất", "đề_xuất");
        put("Báo cáo", "báo_cáo");
        put("Phản hồi", "phản_hồi");
        put("Khiếu nại", "khiếu_nại");
    }};

    private static final int DEFAULT_PAGE_SIZE = 10;

    @GetMapping
    public String listReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        size = (size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Page<ProductReport> reportPage = service.getReportsPaginated(page, size);

        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("totalItems", reportPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("titleMapping", titleMapping);

        return "list";
    }

    // Lọc báo cáo với phân trang
    @GetMapping("/filter")
    public String filterReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        size = (size <= 0) ? DEFAULT_PAGE_SIZE : size;

        // Chuyển đổi title từ display value sang database value
        String dbTitle = null;
        if (title != null && !title.isEmpty()) {
            dbTitle = titleMapping.get(title);
        }

        Page<ProductReport> reportPage = service.filterReportsWithPagination(status, dbTitle, startDate, endDate, page, size);

        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("totalItems", reportPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentFilter", status);
        model.addAttribute("currentTitle", title);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("titleMapping", titleMapping);

        return "list";
    }

    // XEM CHI TIẾT BÁO CÁO
    @GetMapping("/view/{id}")
    public String viewReportDetail(@PathVariable Long id, Model model) {
        try {
            ProductReport report = service.getReportById(id);
            if (report != null) {
                model.addAttribute("report", report);
                ProductReportService.ReportStatistics statistics = service.getReportStatistics();
                model.addAttribute("statistics", statistics);
                model.addAttribute("titleMapping", titleMapping);
                return "report-detail";
            } else {
                return "redirect:/history?error=Báo+cáo+không+tồn+tại";
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            // Thêm reverse mapping để hiển thị đúng giá trị đã chọn
            Map<String, String> reverseMapping = new HashMap<>();
            for (Map.Entry<String, String> entry : titleMapping.entrySet()) {
                reverseMapping.put(entry.getValue(), entry.getKey());
            }
            model.addAttribute("reverseMapping", reverseMapping);

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

            // Chuyển đổi title từ display value sang database value
            String displayTitle = updatedReport.getTitle();
            String dbTitle = titleMapping.get(displayTitle);
            if (dbTitle != null) {
                existingReport.setTitle(dbTitle);
            } else {
                existingReport.setTitle(displayTitle);
            }

            // Chỉ cập nhật các trường mà customer được phép sửa
            existingReport.setName(updatedReport.getName());
            existingReport.setEmail(updatedReport.getEmail());
            existingReport.setProductId(updatedReport.getProductId());
            existingReport.setMessage(updatedReport.getMessage());
            existingReport.setManagerMsg(updatedReport.getManagerMsg());

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
}