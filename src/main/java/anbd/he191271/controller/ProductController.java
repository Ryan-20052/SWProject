package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Categories;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.service.ProductService;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String homepage(Model model) {
        List<Product> products = productService.findAllProducts();
        List<Categories> categories = categoryRepository.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        return "homepage"; // homepage.html
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable int id, Model model) {
        Product product = productService.findProductById(id);
        model.addAttribute("product", product);
        return "product"; // product.html
    }
}
