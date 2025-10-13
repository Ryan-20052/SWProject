package anbd.he191271.controller;

import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/home")
@Controller
public class HomeController {

    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository<P, Number> productRepository;

    public HomeController(ProductService productService,
                          CustomerRepository customerRepository,
                          CategoryRepository categoryRepository,
                          ProductRepository<P, Number> productRepository) {
        this.productService = productService;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
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

        model.addAttribute("products", productPage.getContent());
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
}
