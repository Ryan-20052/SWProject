package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@RequestMapping("/home")
@Controller
public class HomeController {

    private final ProductService productService;
    private final CustomerRepository customerRepository;

    public HomeController(ProductService productService,
                          CustomerRepository customerRepository) {
        this.productService = productService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/homepage")
    public String homepage(Model model, HttpSession session) {
        model.addAttribute("products", productService.findAllProducts());

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) {
            model.addAttribute("customer", customer);
        }
        return "homepage";
    }
}
