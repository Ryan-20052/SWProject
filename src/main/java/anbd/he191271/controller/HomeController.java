package anbd.he191271.controller;

import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
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
    private final ProductRepository productRepository;

    public HomeController(ProductService productService,
                          CustomerRepository customerRepository,
                          CategoryRepository categoryRepository,
                          ProductRepository productRepository) {
        this.productService = productService;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Đảm bảo danh sách categories luôn có sẵn cho mọi view do controller này trả về.
     * (Giúp template ${categories} không bị "Cannot resolve symbol 'categories'")
     */
    @ModelAttribute("categories")
    public List<Categories> populateCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/homepage")
    public String homepage(Model model, HttpSession session) {
        model.addAttribute("products", productService.findAllProducts());

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);
        return "homepage";
    }

    /**
     * Lấy sản phẩm theo category ID và trả view homepage (giữ nguyên layout).
     * URL ví dụ: /home/category/1
     */
    @GetMapping("/category/{id}")
    public String productsByCategory(@PathVariable("id") Integer id, Model model, HttpSession session) {

        // Lấy products theo category id — chọn method phù hợp trong ProductRepository:
        // 1) nếu dùng JPQL method findByCategoryId:
        try {
            model.addAttribute("products", productRepository.findByCategoryId(id));
        } catch (Exception ex) {
            // Nếu JPQL không có (không mapped), thử native method tên findByCategoryIdNative
            model.addAttribute("products", productRepository.findByCategoryIdNative(id));
        }

        // Đặt currentCategory để thymeleaf có thể highlight mục active
        Optional<Categories> current = categoryRepository.findById(id);
        current.ifPresent(c -> model.addAttribute("currentCategory", c));

        // Giữ thông tin customer nếu login để header hiển thị
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);

        return "homepage";
    }
}
