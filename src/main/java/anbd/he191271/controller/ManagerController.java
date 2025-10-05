package anbd.he191271.controller;

import anbd.he191271.dto.ProductDTO;
import anbd.he191271.dto.VariantDTO;
import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Manager;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.service.CategoryService;
import anbd.he191271.service.ProductService;
import anbd.he191271.service.VariantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/manage")
public class ManagerController {

    @Autowired
    private VariantService variantService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/manageHome")
    public String manageHome(Model model, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        model.addAttribute("productListAvai", productService.getAllProductByStatus("available"));
        model.addAttribute("productList", productService.getAllProduct());
        model.addAttribute("newProduct", new ProductDTO());
        model.addAttribute("newVariant", new VariantDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("manager", manager);
        return "manageHome";
    }

    @PostMapping("/addProduct")
    public String addProduct(@ModelAttribute("newProduct") ProductDTO request, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        Categories category = categoryService.getCategoryById(request.getCategoryId());
        Product product=new Product(request.getName(), manager.getId(), request.getImgUrl(),category);
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("newProduct") ProductDTO request, HttpSession session, RedirectAttributes redirectAttributes) {
        Product product = productService.findProductById(request.getProductId());
        Manager manager=(Manager)session.getAttribute("manager");
        product.setName(request.getName());
        product.setManager_id(manager.getId());
        product.setImg_url(request.getImgUrl());
        product.setCategory(categoryService.getCategoryById(request.getCategoryId()));
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("msg", "Đã cập nhật sản phẩm");
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/updateProduct/{id}")
    public String deleteProduct(@PathVariable("id") int id,   Model model, HttpSession session) {
        Product product = productService.findProductById(id);
        ProductDTO request=new ProductDTO(product.getId(), product.getName(), product.getCategory().getId(), product.getImg_url());
        Manager manager=(Manager)session.getAttribute("manager");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("manager", manager);
        model.addAttribute("request", request);
        return "productUpdatePage";
    }

    @PostMapping("/addVariant")
    public String addVariant(@ModelAttribute("newVariant") VariantDTO request, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        Product product = productService.findProductById(request.getProductId());
        Variant variant = new Variant(request.getName(),request.getDuration(),request.getPrice(), product);
        variantService.saveVariant(variant);
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/updateVariant/{id}")
    public String updateVariant(@PathVariable("id") int id,  Model model, HttpSession session) {
        Variant variant = variantService.getVariantById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        model.addAttribute("request", new VariantDTO(variant.getId(),variant.getName(), variant.getDuration(), variant.getPrice(),variant.getProduct().getId()));
        model.addAttribute("manager",manager);
        model.addAttribute("productList", productService.getAllProduct());
        return "variantUpdatePage";
    }

//    @PostMapping("updateVariant")
//    public String updateVariant(@ModelAttribute("newVariant") VariantDTO request, HttpSession session) {
//
//    }

    @GetMapping("/deleteVariant/{id}")
    public String deleteVariant(@PathVariable int id, RedirectAttributes redirectAttributes) {
        variantService.deleteVariant(id);
        redirectAttributes.addFlashAttribute("msg", "Đã xóa thành công");
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/statusProduct/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Product product = productService.findProductById(id);
        if(product.getStatus().equals("available")) {
            product.setStatus("unavailable");
            redirectAttributes.addFlashAttribute("msg", "Đã ẩn sản phẩm khỏi HomePage");
        }else{
            product.setStatus("available");
            redirectAttributes.addFlashAttribute("msg", "Đã bỏ ẩn sản phẩm");
        }
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

}


