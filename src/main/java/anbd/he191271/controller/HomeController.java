package anbd.he191271.controller;

import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.ProductReviewReport;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.service.ProductReviewReportService;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/home")
@Controller
public class HomeController {

    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository<P, Number> productRepository;
    private final ProductReviewReportService reportService;

    public HomeController(ProductService productService,
                          CustomerRepository customerRepository,
                          CategoryRepository categoryRepository,
                          ProductRepository<P, Number> productRepository,
                          ProductReviewReportService reportService) {
        this.productService = productService;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.reportService = reportService; // THÊM ASSIGNMENT
    }

    // luôn cung cấp categories cho mọi view do controller này trả về
    @ModelAttribute("categories")
    public List<Categories> populateCategories() {
        return categoryRepository.findAll();
    }

    // luôn cung cấp danh sách best sellers cho mọi view (nếu bạn chỉ muốn cho homepage remove nếu cần)
    @ModelAttribute("bestSellers")
    public List<Product> populateBestSellers() {
        // đảm bảo ProductService có method getBestSellingProducts(int)
        return productService.getBestSellingProducts(4);
    }

    /**
     * Homepage với phân trang.
     * Parameter:
     *     page (0-based), size
     */
    @GetMapping("/homepage")
    public String homepage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            HttpSession session) {

        Page<Product> productPage = productService.getAllProductByStatusPage("available", page, size);

        model.addAttribute("products", productPage.getContent()); //getContent() — danh sách phần tử trên trang hiện tại (List<T>).
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrev", productPage.hasPrevious());

        // customer (nếu có đăng nhập)
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);

        return "homepage"; // file templates/homepage.html
    }

    @GetMapping("/categories/{id}")
    public String productsByCategories(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            HttpSession session) {

        Page<Product> productPage = productService.getProductsByCategoryPage(id, page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // 🟢 Lấy toàn bộ categories để dropdown luôn có sẵn
        model.addAttribute("categories", categoryRepository.findAll());

        // 🟢 Lấy category hiện tại để đánh dấu selected
        categoryRepository.findById(id)
                .ifPresent(c -> model.addAttribute("currentCategory", c));

        return "homepage";
    }
    /**
     * Lọc theo category có phân trang
     */
    @GetMapping("/category/{id}")
    public String productsByCategory(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            HttpSession session) {

        Page<Product> productPage;
        try {
            productPage = productService.getProductsByCategoryPage(id, page, size);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("totalItems", productPage.getTotalElements());
            model.addAttribute("hasNext", productPage.hasNext());
            model.addAttribute("hasPrev", productPage.hasPrevious());
        } catch (Exception ex) {
            // Fallback: nếu repository chưa hỗ trợ Page (sử dụng method cũ)
            model.addAttribute("products", productRepository.findByCategoryId(id));
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("pageSize", Integer.MAX_VALUE);
            model.addAttribute("totalItems", ((List) model.getAttribute("products")).size());
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrev", false);
        }

        Optional<Categories> current = categoryRepository.findById(id);
        current.ifPresent(c -> model.addAttribute("currentCategory", c));

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);

        return "homepage";
    }
    @GetMapping("/my-reported-reviews")
    public String myReportedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String productSearch,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpSession session,
            Model model) {

        // Kiểm tra đăng nhập
        Customer currentCustomer = (Customer) session.getAttribute("customer");
        if (currentCustomer == null) {
            return "redirect:/login.html";
        }

        // Lấy tất cả reports của user
        List<ProductReviewReport> allReports = reportService.getReportsByReporterId((long) currentCustomer.getId());

        // Áp dụng filters
        List<ProductReviewReport> filteredReports = allReports.stream()
                .filter(report -> status == null || status.isEmpty() || report.getStatus().name().equals(status))
                .filter(report -> reason == null || reason.isEmpty() || report.getReportReason().name().equals(reason))
                .filter(report -> productSearch == null || productSearch.isEmpty() ||
                        report.getProduct().getName().toLowerCase().contains(productSearch.toLowerCase()))
                .filter(report -> startDate == null || !report.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(report -> endDate == null || !report.getCreatedAt().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Phân trang
        int start = page * size;
        int end = Math.min(start + size, filteredReports.size());
        List<ProductReviewReport> pageContent = filteredReports.subList(start, end);

        int totalPages = (int) Math.ceil((double) filteredReports.size() / size);

        // Thống kê
        long pendingCount = allReports.stream().filter(r -> r.getStatus() == ProductReviewReport.ReportStatus.PENDING).count();
        long approvedCount = allReports.stream().filter(r -> r.getStatus() == ProductReviewReport.ReportStatus.APPROVED).count();

        // Tạo filter params để giữ lại filter khi phân trang
        StringBuilder filterParams = new StringBuilder();
        if (status != null && !status.isEmpty()) filterParams.append("&status=").append(status);
        if (reason != null && !reason.isEmpty()) filterParams.append("&reason=").append(reason);
        if (productSearch != null && !productSearch.isEmpty()) filterParams.append("&productSearch=").append(productSearch);
        if (startDate != null) filterParams.append("&startDate=").append(startDate);
        if (endDate != null) filterParams.append("&endDate=").append(endDate);

        model.addAttribute("reports", pageContent);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", filteredReports.size());
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrev", page > 0);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("hasFilters", status != null || reason != null || productSearch != null || startDate != null || endDate != null);
        model.addAttribute("filterParams", filterParams.toString());

        // Giữ lại filter values
        model.addAttribute("status", status);
        model.addAttribute("reason", reason);
        model.addAttribute("productSearch", productSearch);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "my-reported-reviews";
    }
}
