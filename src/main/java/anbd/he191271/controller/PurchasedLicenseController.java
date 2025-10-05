package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.LicenseKeyRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PurchasedLicenseController {

    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    @GetMapping("/purchasedlicenses")
    public String showPurchasedLicenses(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");

        // Nếu chưa đăng nhập → chuyển sang login
        if (customer == null) {
            model.addAttribute("message", "Vui lòng đăng nhập để xem license đã mua.");
            return "redirect:/login.html"; // file login.html
        }

        // Lấy danh sách license theo customer
        List<LicenseKey> purchasedLicenses = licenseKeyRepository.findAllByCustomerId(customer.getId());
        model.addAttribute("purchasedLicenses", purchasedLicenses);

        return "purchasedlicenses"; // file Thymeleaf purchasedlicenses.html
    }
}
