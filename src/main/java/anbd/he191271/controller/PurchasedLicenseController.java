package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.LicenseKey;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.LicenseKeyRepository;
import anbd.he191271.service.LicenseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PurchasedLicenseController {

    @Autowired
    private LicenseKeyRepository licenseKeyRepository;

    @Autowired
    private LicenseService licenseService;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/purchasedlicenses")
    public String showPurchasedLicenses(
            HttpSession session,
            Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "sort", defaultValue = "newest") String sort, // THÊM PARAM SORT
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "status", defaultValue = "ACTIVATE") String status
    ) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        // Cập nhật license hết hạn
        licenseService.updateExpiredLicenses();

        List<LicenseKey> allLicenses = licenseKeyRepository.findAllByCustomerId(customer.getId());

        // lọc theo khoảng thời gian
        if (from != null && to != null) {
            allLicenses = allLicenses.stream()
                    .filter(l -> {
                        if (l.getActivatedAt() == null) return false;
                        // Sửa phần convert LocalDateTime sang LocalDate
                        LocalDate activatedDate = l.getActivatedAt().toLocalDate();
                        return !activatedDate.isBefore(from) && !activatedDate.isAfter(to);
                    })
                    .toList();
        }

        // tìm kiếm theo tên sản phẩm
        if (search != null && !search.isBlank()) {

            allLicenses = allLicenses.stream()
                    .filter(l -> l.getOrderDetail().getVariant().getProduct()
                            .getName().toLowerCase().contains(search.toLowerCase().trim()))
                    .toList();
        }

        // lọc theo category
        if (category != null && !category.isBlank()) {
            allLicenses = allLicenses.stream()
                    .filter(l -> l.getOrderDetail().getVariant().getProduct()
                            .getCategory().getName().equalsIgnoreCase(category))
                    .toList();
        }

        // lọc theo trạng thái
        if(status !=null && !status.isEmpty()){
            allLicenses = allLicenses.stream()
                    .filter(l -> l.getStatus().equals(status))
                    .toList();
        }

        // THÊM PHẦN SẮP XẾP
        allLicenses = sortLicenses(allLicenses, sort);

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
        model.addAttribute("sort", sort); // THÊM SORT VÀO MODEL
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("session", session);
        return "purchasedlicenses";
    }

    // THÊM METHOD SẮP XẾP
    private List<LicenseKey> sortLicenses(List<LicenseKey> licenses, String sort) {
        return licenses.stream()
                .sorted((l1, l2) -> {
                    switch (sort) {
                        case "name_asc":
                            return l1.getOrderDetail().getVariant().getProduct()
                                    .getName().compareToIgnoreCase(
                                            l2.getOrderDetail().getVariant().getProduct().getName());
                        case "name_desc":
                            return l2.getOrderDetail().getVariant().getProduct()
                                    .getName().compareToIgnoreCase(
                                            l1.getOrderDetail().getVariant().getProduct().getName());
                        case "oldest":
                            return l1.getActivatedAt().compareTo(l2.getActivatedAt());
                        case "newest":
                        default:
                            return l2.getActivatedAt().compareTo(l1.getActivatedAt());
                    }
                })
                .toList();
    }
}