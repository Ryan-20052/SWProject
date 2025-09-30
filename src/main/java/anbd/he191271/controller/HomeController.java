package anbd.he191271.controller;

import anbd.he191271.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/home")
@Controller
public class HomeController {

    private final ProductService productService;

    // Constructor injection
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/homepage")
    public String homepage(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        return "homepage"; // trả về file homepage.html trong templates
    }
}
