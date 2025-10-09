package anbd.he191271.controller;

import anbd.he191271.dto.ProductDTO;
import anbd.he191271.dto.VariantDTO;
import anbd.he191271.entity.*;
import anbd.he191271.service.CategoryService;
import anbd.he191271.service.ManagerLogService;
import anbd.he191271.service.ProductService;
import anbd.he191271.service.VariantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/manage")
public class ManagerController {

    @Autowired
    private VariantService variantService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ManagerLogService managerLogService;

    @GetMapping("/manageHome")
    public String manageHome(Model model, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
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
        if (manager == null) {
            return "redirect:/login.html";
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "add product "+ request.getName());
        managerLogService.save(log);
        Categories category = categoryService.getCategoryById(request.getCategoryId());
        Product product=new Product(request.getName(), manager.getId(), request.getImgUrl(),category, request.getDescription());
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("request") ProductDTO request, HttpSession session, RedirectAttributes redirectAttributes) {
        Product product = productService.findProductById(request.getProductId());
        Manager manager=(Manager)session.getAttribute("manager");
        product.setName(request.getName());
        product.setManager_id(manager.getId());
        product.setImg_url(request.getImgUrl());
        product.setDescription(request.getDescription());
        product.setCategory(categoryService.getCategoryById(request.getCategoryId()));
        productService.saveProduct(product);
        ManagerLog log =  new ManagerLog(manager.getUsername(), "update product "+ request.getName());
        managerLogService.save(log);
        redirectAttributes.addFlashAttribute("msg", "Đã cập nhật sản phẩm");
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/updateProduct/{id}")
    public String deleteProduct(@PathVariable("id") int id,   Model model, HttpSession session) {
        Product product = productService.findProductById(id);
        ProductDTO request=new ProductDTO(product.getId(), product.getName(), product.getCategory().getId(), product.getImg_url(), product.getDescription());
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("manager", manager);
        model.addAttribute("request", request);
        return "productUpdatePage";
    }

    @PostMapping("/addVariant")
    public String addVariant(@ModelAttribute("newVariant") VariantDTO request, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        Product product = productService.findProductById(request.getProductId());
        Variant variant = new Variant(request.getName(),request.getDuration(),request.getPrice(), product);
        variantService.saveVariant(variant);
        ManagerLog log =  new ManagerLog(manager.getUsername(), "add variant "+ request.getName());
        managerLogService.save(log);
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/updateVariant/{id}")
    public String updateVariant(@PathVariable("id") int id,  Model model, HttpSession session) {
        Variant variant = variantService.getVariantById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("request", new VariantDTO(variant.getId(),variant.getName(), variant.getDuration(), variant.getPrice(),variant.getProduct().getId()));
        model.addAttribute("manager",manager);
        model.addAttribute("productList", productService.getAllProduct());
        return "variantUpdatePage";
    }

    @PostMapping("updateVariant")
    public String updateVariant(@ModelAttribute("request") VariantDTO request, HttpSession session, RedirectAttributes redirectAttributes) {
        Variant variant =  variantService.getVariantById(request.getId());
        Manager manager=(Manager)session.getAttribute("manager");
        variant.setName(request.getName());
        variant.setDuration(request.getDuration());
        variant.setPrice(request.getPrice());
        variant.setProduct(productService.findProductById(request.getProductId()));
        variantService.saveVariant(variant);
        ManagerLog log =  new ManagerLog(manager.getUsername(), "update variant "+ request.getName());
        managerLogService.save(log);
        return "redirect:/manage/manageHome";
    }


    @GetMapping("/statusProduct/{id}")
    public String updateStatusProduct(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session) {
        Product product = productService.findProductById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(product.getStatus().equals("available")) {
            product.setStatus("unavailable");
            redirectAttributes.addFlashAttribute("msg", "Đã ẩn sản phẩm khỏi HomePage");
        }else{
            product.setStatus("available");
            redirectAttributes.addFlashAttribute("msg", "Đã bỏ ẩn sản phẩm");
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "change status of product "+ product.getName());
        managerLogService.save(log);
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/statusVariant/{id}")
    public String updateStatusVariant(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session) {
        Variant variant = variantService.getVariantById(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(variant.getStatus().equals("available")) {
            variant.setStatus("unavailable");
            redirectAttributes.addFlashAttribute("msg", "Đã ẩn sản phẩm khỏi HomePage");
        }else{
            variant.setStatus("available");
            redirectAttributes.addFlashAttribute("msg", "Đã bỏ ẩn sản phẩm");
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "change status of variant "+ variant.getName());
        managerLogService.save(log);
        variantService.saveVariant(variant);
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/manageLog")
    public String manageLog(Model model, HttpSession session) {
        List<ManagerLog> manageLogList = managerLogService.findAll();
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("manager", manager);
        model.addAttribute("manageLogList", manageLogList.reversed());
        return "manageLogPage";
    }

}


