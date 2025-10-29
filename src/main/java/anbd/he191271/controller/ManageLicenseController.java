package anbd.he191271.controller;

import anbd.he191271.entity.*;
import anbd.he191271.service.LicenseService;
import anbd.he191271.service.ManagerLogService;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/manageLicense")
public class ManageLicenseController {

    @Autowired
    private LicenseService licenseService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ManagerLogService managerLogService;

    @GetMapping("/viewLicense")
    public String viewLicense(@RequestParam(required = false) String customerName,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              @RequestParam(required = false) String productName,
                              @RequestParam(required = false) String variantName,
                              @RequestParam(required = false) String sort,
                              Model model, HttpSession session) {
        Manager manager = (Manager)session.getAttribute("manager");
        Stream<LicenseKey> stream = licenseService.getAllLicense().stream();

        if (manager == null) {
            return "redirect:/login.html";
        }

        if(customerName != null && !customerName.trim().isEmpty()) {
            stream = stream.filter(l -> l.getOrderDetail().getOrder().getCustomer().getName().toLowerCase().contains(customerName.trim()));
            model.addAttribute("customerName", customerName);
        }
        if(productName != null && !productName.trim().isEmpty()) {
            stream = stream.filter(l -> l.getOrderDetail().getVariant().getProduct().getName().equals(productName));
            model.addAttribute("productName", productName);
        }
        if(variantName != null && !variantName.trim().isEmpty()) {
            stream = stream.filter(l -> l.getOrderDetail().getVariant().getName().toLowerCase().contains(variantName.trim()));
            model.addAttribute("variantName", variantName);
        }
        if(status != null && !status.trim().isEmpty()) {
            stream = stream.filter(l -> l.getStatus().equals(status));
            model.addAttribute("status", status);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            stream = stream.filter(l ->
                    !l.getActivatedAt().toLocalDate().isBefore(start)
            );
            model.addAttribute("startDate", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            stream = stream.filter(l ->
                    !l.getActivatedAt().toLocalDate().isAfter(end)
            );
            model.addAttribute("endDate", endDate);
        }
        if(sort != null && !sort.trim().isEmpty()) {
            if(sort.equals("date_asc")){
                stream = stream.sorted(Comparator.comparing(LicenseKey :: getActivatedAt));
            }else if(sort.equals("date_desc")){
                stream = stream.sorted(Comparator.comparing(LicenseKey :: getActivatedAt).reversed());
            }
            model.addAttribute("sort", sort);
        }
        List<String> statusList = List.of("ACTIVATE","BANNED");
        model.addAttribute("productList",productService.getAllProduct());
        model.addAttribute("statusList", statusList);
        model.addAttribute("manager", manager);
        model.addAttribute("licenseList", stream.toList());
        return "manageLicense";
    }

    @PostMapping("/statusLicense")
    public String updateStatusVariant(@RequestParam int id, RedirectAttributes redirectAttributes, HttpSession session) {
        LicenseKey license = licenseService.getLicenseById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(license.getStatus().equals("ACTIVATE")) {
            license.setStatus("BANNED");
            redirectAttributes.addFlashAttribute("msg", "Đã chặn quyền sử dụng license key");
        }else if(license.getStatus().equals("BANNED")){
            license.setStatus("ACTIVATE");
            redirectAttributes.addFlashAttribute("msg", "Đã bỏ chặn quyền sử dụng license key");
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "change status license key Customer:  "+ license.getOrderDetail().getOrder().getCustomer().getName());
        managerLogService.save(log);
        licenseService.save(license);
        return "redirect:/manageLicense/viewLicense";
    }

    @PostMapping("/deleteLicense")
    public String deleteLicense(@RequestParam int id, RedirectAttributes redirectAttributes, HttpSession session) {
        LicenseKey license = licenseService.getLicenseById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(license.getStatus().equals("BANNED")){
            licenseService.delete(license);
            redirectAttributes.addFlashAttribute("msg", "Đã xóa license key");
        }else{
            return "redirect:/manageLicense/viewLicense";
        }
        return "redirect:/manageLicense/viewLicense";
    }
}
