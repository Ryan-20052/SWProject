package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.LicenseKeyRepository;
import anbd.he191271.service.PurchaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    @PostMapping("/purchase")
    public String purchaseVariant(
            @RequestParam("productId") int productId,
            @RequestParam("variantId") int variantId,
            @RequestParam("amount") int amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Lấy customer từ session
            Customer customer = (Customer) session.getAttribute("customer");

            if (customer == null) {
                redirectAttributes.addFlashAttribute("message", "⚠️ Vui lòng đăng nhập trước khi mua!");
                return "redirect:/product/" + productId;
            }

            // Nếu đã login thì xử lý mua
            purchaseService.purchaseVariant(
                    variantId,
                    customer.getId(),
                    customer.getEmail(),
                    amount
            );

            redirectAttributes.addFlashAttribute("message", "✅ Mua hàng thành công! License key đã được gửi tới email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/product/" + productId;
    }

    @GetMapping("/purchasedlicenses")
    public String purchasedLicenses(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("message", "⚠️ Vui lòng đăng nhập trước!");
            return "redirect:/login.html"; // 🔥 ép về login.html
        }

        List<LicenseKey> purchasedLicenses = licenseKeyRepository.findAllByCustomerId(customer.getId());
        model.addAttribute("purchasedLicenses", purchasedLicenses);

        return "purchasedlicenses";
    }


}
