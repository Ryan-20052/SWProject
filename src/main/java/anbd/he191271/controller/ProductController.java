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

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.findProductById(id); // đã có trong service
        model.addAttribute("product", product);
        model.addAttribute("variants", product.getVariants());
        return "product"; // => product.html
    }

}
