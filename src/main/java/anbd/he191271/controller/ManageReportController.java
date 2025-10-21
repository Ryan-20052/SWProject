package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.entity.ProductReport;
import anbd.he191271.service.ProductReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/manageReport")
public class ManageReportController {

    @Autowired
    ProductReportService productReportService;

    @GetMapping("/viewReport")
    public String viewReport(Model model, HttpSession session,
                             String customerName,
                             String title,
                             String type,
                             String status,
                             String startDate,
                             String endDate) {
        Stream<ProductReport> stream = productReportService.getAllReports().stream();
        if (status != null) {
            if(status.equalsIgnoreCase("pending")){
                stream = stream.filter(r -> r.getStatus().equalsIgnoreCase("pending"));
            }else{
                stream = stream.filter(r -> r.getStatus().equalsIgnoreCase(status));
                List<String> statusList = List.of("approved", "rejected") ;
                model.addAttribute("statusList", statusList);
                model.addAttribute("status", status);
            }
        }else{
            stream = stream.filter(r -> !r.getStatus().equalsIgnoreCase("pending"));
            List<String> statusList = List.of("approved", "rejected") ;
            model.addAttribute("statusList", statusList);
            model.addAttribute("status", status);
        }
        if (customerName != null && !customerName.trim().isEmpty()) {
            stream = stream.filter(r -> r.getName().toLowerCase().contains(customerName.toLowerCase()));
        }
        if (title != null && !title.trim().isEmpty()) {
            stream = stream.filter(r -> r.getTitle().equalsIgnoreCase(title));
        }
        if (type != null && !type.trim().isEmpty()) {
            stream = stream.filter(r -> r.getType().equalsIgnoreCase(type));
        }
        if (startDate !=null && !startDate.trim().isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            stream = stream.filter(r -> !r.getCreatedAt().toLocalDate().isBefore(start));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            stream = stream.filter(r -> !r.getCreatedAt().toLocalDate().isAfter(end));
        }
        Manager manager  = (Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("customerName", customerName);
        model.addAttribute("title", title);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("manager", manager);
        model.addAttribute("reportList", stream.toList());
        return "manageReport";
    }

    @GetMapping("/viewPendingReport")
    public String viewPendingReport(Model model, HttpSession session,
                                  @RequestParam(required = false) String customerName,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate){
        String status = "pending";
        return viewReport(model, session, customerName, title, type, status, startDate, endDate);
    }

    @GetMapping("/viewHandledReport")
    public String viewHandledReport(Model model, HttpSession session,
                                  @RequestParam(required = false) String customerName,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate){
        return viewReport(model, session, customerName, title, type, status, startDate, endDate);
    }

    @GetMapping("/reportDetail/{id}")
    public String reportDetail(@PathVariable Long id, Model model, HttpSession session) {
        ProductReport report = productReportService.getReportById(id);
        Manager manager  = (Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("manager", manager);
        model.addAttribute("report", report);
        return "reportDetail";
    }

    @PostMapping("/reportAction")
    public String reportAction(@RequestParam String managerMsg,
                               @RequestParam String action,
                               @RequestParam Long reportId,
                               RedirectAttributes redirectAttributes) {
        ProductReport report = productReportService.getReportById(reportId);
        report.setManagerMsg(managerMsg);
        report.setStatus(action);
        productReportService.saveReport(report);
        redirectAttributes.addFlashAttribute("msg", "đã gửi phản hồi cho khách hàng");
        return "redirect:/manageReport/viewReport";
    }
}
