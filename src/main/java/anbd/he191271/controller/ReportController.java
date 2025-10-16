package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.ProductReport;
import anbd.he191271.repository.ReportRepository;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/report")
public class ReportController {
    private final ProductService productService;
    private final ReportRepository reportRepository;
    private final CustomerRepository customerRepository;

    public ReportController(ProductService productService, ReportRepository reportRepository, CustomerRepository customerRepository) {
        this.productService = productService;
        this.reportRepository = reportRepository;
        this.customerRepository = customerRepository;
    }

    // hiển thị form (template: report.html)
    @GetMapping("/view")
    public String viewSupportPage(Model model, HttpSession session) {
        // Luôn load products trước (để template luôn có)
        List<Product> products = productService.getAllProductByStatus("available");
        model.addAttribute("products", products);

        // Nếu có customer object trong session thì thêm vào model (không return sớm)
        Object sessCustomer = session.getAttribute("customer");
        if (sessCustomer instanceof Customer) {
            model.addAttribute("customer", (Customer) sessCustomer);
        } else {
            // nếu không có object thì thử lấy customerId (các tên key khác nhau)
            Integer customerId = getCustomerIdFromSession(session);
            if (customerId != null) {
                Optional<Customer> customerOpt = customerRepository.findById(customerId);
                customerOpt.ifPresent(customer -> model.addAttribute("customer", customer));
            }
        }

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        ProductReport r = new ProductReport();
        r.setTitle(subject);
        r.setMessage(message);
        r.setName(name);
        r.setEmail(email);
        r.setType("support");
        r.setProductId(productId);
        r.setStatus("pending");

        // Dùng helper để lấy customer id (tìm cả "customerId", "customer_id", hoặc object "customer")
        Integer customerId = getCustomerIdFromSession(session);

        if (customerId != null) {
            r.setCustomer_id(customerId); // giữ tên setter giống entity hiện tại của bạn
        }

        reportRepository.save(r);

        redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu thành công. Chúng tôi sẽ phản hồi sớm.");
        return "redirect:/report/view";
    }

    // helper: lấy customerId từ session theo nhiều kiểu key
    private Integer getCustomerIdFromSession(HttpSession session) {
        // Thử các key khác nhau mà app có thể dùng
        Object v = session.getAttribute("customerId");
        Integer id = parseIntegerFromSession(v);
        if (id != null) return id;

        v = session.getAttribute("customer_id");
        id = parseIntegerFromSession(v);
        if (id != null) return id;

        v = session.getAttribute("customer"); // object Customer
        if (v instanceof Customer) {
            Object idObj = ((Customer) v).getId();
            if (idObj instanceof Number) {
                long val = ((Number) idObj).longValue();
                if (val <= Integer.MAX_VALUE && val >= Integer.MIN_VALUE) {
                    return (int) val;
                }
            } else if (idObj instanceof String) {
                try {
                    return Integer.parseInt((String) idObj);
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    // helper: parse Integer từ các kiểu có thể được lưu trong session
    private Integer parseIntegerFromSession(Object cid) {
        if (cid == null) return null;
        if (cid instanceof Integer) return (Integer) cid;
        if (cid instanceof Long) {
            Long l = (Long) cid;
            if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) {
                return l.intValue();
            } else {
                return null; // ID quá lớn cho Integer
            }
        }
        if (cid instanceof String) {
            try {
                return Integer.parseInt((String) cid);
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }
}