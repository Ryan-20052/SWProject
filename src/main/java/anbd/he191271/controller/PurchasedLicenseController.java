package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.LicenseKeyRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PurchasedLicenseController {

    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    @GetMapping("/purchasedlicenses")
    public String showPurchasedLicenses(
            HttpSession session,
            Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        List<LicenseKey> allLicenses = licenseKeyRepository.findAllByCustomerId(customer.getId());

        // lọc theo khoảng thời gian
        if (from != null && to != null) {
            allLicenses = allLicenses.stream()
                    .filter(l -> {
                        if (l.getActivatedAt() == null) return false;
                        LocalDate activatedDate = l.getActivatedAt()
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return !activatedDate.isBefore(from) && !activatedDate.isAfter(to);
                    })
                    .toList();
        }


        // tìm kiếm theo tên sản phẩm
        if (search != null && !search.isBlank()) {
            allLicenses = allLicenses.stream()
                    .filter(l -> l.getOrderDetail().getVariant().getProduct()
                            .getName().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        // lọc theo category
        if (category != null && !category.isBlank()) {
            allLicenses = allLicenses.stream()
                    .filter(l -> l.getOrderDetail().getVariant().getProduct()
                            .getCategory().getName().equalsIgnoreCase(category))
                    .toList();
        }

        // phân trang (10 license / trang)
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) allLicenses.size() / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, allLicenses.size());
        List<LicenseKey> pageLicenses = allLicenses.subList(fromIndex, toIndex);

        model.addAttribute("purchasedLicenses", pageLicenses);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "purchasedlicenses";
    }

}
