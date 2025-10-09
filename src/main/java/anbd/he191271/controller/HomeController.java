package anbd.he191271.controller;

import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
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

    @GetMapping("/homepage")
    public String homepage(Model model, HttpSession session) {
        // products chính (tất cả)
        model.addAttribute("products", productService.getAllProductByStatus("available"));

        // customer (nếu có đăng nhập)
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);

        return "homepage"; // file templates/homepage.html
    }

    @GetMapping("/category/{id}")
    public String productsByCategory(@PathVariable("id") Integer id, Model model, HttpSession session) {
        // Lấy products theo category id
        try {
            model.addAttribute("products", productRepository.findByCategoryId(id));
        } catch (Exception ex) {
            model.addAttribute("products", productRepository.findByCategoryIdNative(id));
        }

        Optional<Categories> current = categoryRepository.findById(id);
        current.ifPresent(c -> model.addAttribute("currentCategory", c));

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) model.addAttribute("customer", customer);

        return "homepage";
    }



}
