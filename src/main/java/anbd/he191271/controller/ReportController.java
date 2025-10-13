package anbd.he191271.controller;

import anbd.he191271.entity.ProductReport;
import anbd.he191271.repository.ReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // hiển thị form (template: report.html)
    @GetMapping("/view")
    public String viewSupportPage() {
        return "report";
    }

    // xử lý submit form -> lưu vào productReport
    @PostMapping("/submit")
    public String submitReport(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "productId", required = false) Long productId,
            RedirectAttributes redirectAttributes) {

        ProductReport r = new ProductReport();
        r.setTitle(subject);
        r.setMessage(message);
        r.setName(name);
        r.setEmail(email);
        r.setType("support");
        r.setProductId(productId);
        r.setStatus("pending");
        // createdAt/updatedAt sẽ do @PrePersist/@PreUpdate set

        reportRepository.save(r);

        redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu thành công. Chúng tôi sẽ phản hồi sớm.");
        return "redirect:/report/view";
    }
}
