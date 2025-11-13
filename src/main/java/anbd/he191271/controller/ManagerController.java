package anbd.he191271.controller;

import anbd.he191271.dto.CategoryDTO;
import anbd.he191271.dto.ProductDTO;
import anbd.he191271.dto.VariantDTO;
import anbd.he191271.entity.*;
import anbd.he191271.service.CategoryService;
import anbd.he191271.service.ManagerLogService;
import anbd.he191271.service.ProductService;
import anbd.he191271.service.VariantService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;


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
    public String manageHome(
            @RequestParam(name = "name",required = false) String productName,
            @RequestParam(name ="categoryId", required = false) String categoryId,
            @RequestParam(required = false) String status,
            Model model, HttpSession session)
    {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        Stream<Product> stream=productService.getAllProduct().stream();
        if(productName!=null&&!productName.isEmpty()){
            stream=stream.filter(p -> p.getName().toLowerCase().contains(productName.toLowerCase().trim()));
            model.addAttribute("productName",productName);
        }
        if(categoryId!=null&&!categoryId.isEmpty()){
            stream=stream.filter(p -> p.getCategory().getId()==Integer.parseInt(categoryId));
            model.addAttribute("categoryId",Integer.parseInt(categoryId));
        }
        if(status!=null&&!status.isEmpty()){
            stream=stream.filter(p -> p.getStatus().equals(status));
            model.addAttribute("status",status);
        }
        model.addAttribute("productListAvai", productService.getAllProductByStatus("available"));
        model.addAttribute("productList", stream.toList());
        model.addAttribute("newProduct", new ProductDTO());
        model.addAttribute("newVariant", new VariantDTO());
        model.addAttribute("categories", categoryService.getAllCategoriesByStatus("available"));
        model.addAttribute("manager", manager);
        return "manageHome";
    }

    @PostMapping("/addProduct")
    public String addProduct(@Valid @ModelAttribute("newProduct") ProductDTO request, BindingResult bindingResult,HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(bindingResult.hasErrors()){
            model.addAttribute("productListAvai", productService.getAllProductByStatus("available"));
            model.addAttribute("productList", productService.getAllProduct());
            model.addAttribute("newVariant", new VariantDTO());
            model.addAttribute("categories", categoryService.getAllCategoriesByStatus("available"));
            model.addAttribute("manager", manager);
            return "manageHome";
        }
        if(isProductDup(request)){
            redirectAttributes.addFlashAttribute("msg", "Sản phẩm đã tồn tại");
            return "redirect:/manage/manageHome";
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "add product "+ request.getName());
        managerLogService.save(log);
        Categories category = categoryService.getCategoryById(request.getCategoryId());
        Product product=new Product(request.getName(), manager.getId(), request.getImgUrl(),category, request.getDescription());
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    public boolean isProductDup(ProductDTO product){
        List<Product> productList =  productService.getAllProduct();
        for(Product p : productList){
            if(product.getProductId() == null){
                if(p.getName().equalsIgnoreCase(product.getName())){
                    return true;
                }
            }else{
                if (p.getId() == product.getProductId()) {
                    continue;
                }
                if(p.getName().equalsIgnoreCase(product.getName())){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVariantDup(VariantDTO variant){
        List<Variant> variantList =  variantService.getAllVariant();
        for(Variant v : variantList){
            if(variant.getId() == null){
                if(v.getName().equalsIgnoreCase(variant.getName())){
                    return true;
                }
                if(v.getDuration().equalsIgnoreCase(variant.getDuration()) && v.getProduct().getId() == variant.getProductId()){
                    return true;
                }
            }else{
                if (v.getId() == variant.getId()) {
                    continue;
                }
                if(v.getName().equalsIgnoreCase(variant.getName())){
                    return true;
                }
                if(v.getDuration().equalsIgnoreCase(variant.getDuration()) && v.getProduct().getId() == variant.getProductId()){
                    return true;
                }
            }
        }
        return false;
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@Valid @ModelAttribute("request") ProductDTO request, BindingResult bindingResult,  HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Product product = productService.findProductById(request.getProductId());
        Manager manager=(Manager)session.getAttribute("manager");
        if(bindingResult.hasErrors()){
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("manager", manager);
            model.addAttribute("request", request);
            return "productUpdatePage";
        }
        if(isProductDup(request)){
            redirectAttributes.addFlashAttribute("msg", "sản phẩm đã tồn tại");
            return "redirect:/manage/updateProduct/" + request.getProductId();
        }
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
    public String addVariant(@Valid @ModelAttribute("newVariant") VariantDTO request, BindingResult bindingResult, HttpSession session, Model  model,RedirectAttributes redirectAttributes) {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("productListAvai", productService.getAllProductByStatus("available"));
            model.addAttribute("productList", productService.getAllProduct());
            model.addAttribute("newProduct", new ProductDTO());
            model.addAttribute("categories", categoryService.getAllCategoriesByStatus("available"));
            model.addAttribute("manager", manager);
            return "manageHome";
        }
        if(isVariantDup(request)){
            redirectAttributes.addFlashAttribute("msg", "Gói sản phẩm đã tồn tại");
            return "redirect:/manage/manageHome";
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
    public String updateVariant(@Valid @ModelAttribute("request") VariantDTO request, BindingResult bindingResult, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Variant variant =  variantService.getVariantById(request.getId());
        Manager manager=(Manager)session.getAttribute("manager");
        if (bindingResult.hasErrors()) {
            model.addAttribute("manager",manager);
            model.addAttribute("productList", productService.getAllProduct());
            return "variantUpdatePage";
        }
        if(isVariantDup(request)){
            redirectAttributes.addFlashAttribute("msg", "Gói sản phẩm đã tồn tại");
            return "redirect:/manage/updateVariant/" + request.getId();
        }
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
        List<Variant> variantList = variantService.getVariantsByProduct(id);
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(product.getStatus().equals("available")) {
            product.setStatus("unavailable");
            variantList.forEach(v -> v.setStatus("unavailable"));
            redirectAttributes.addFlashAttribute("msg", "Đã ẩn sản phẩm khỏi HomePage");
        }else{
            product.setStatus("available");
            variantList.forEach(v -> v.setStatus("available"));
            redirectAttributes.addFlashAttribute("msg", "Đã bỏ ẩn sản phẩm");
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "change status of product "+ product.getName());
        managerLogService.save(log);
        variantService.saveAll(variantList);
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
    public String manageLog(Model model, HttpSession session,
                            @RequestParam(required = false) String managerName,
                            @RequestParam(required = false) String action,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {
        Stream<ManagerLog> stream = managerLogService.findAll().stream();

        if(managerName!= null && !managerName.trim().isEmpty()) {
            stream = stream.filter(l -> l.getManagerName().toLowerCase().contains(managerName.toLowerCase().trim()));
        }
        if(action != null && !action.trim().isEmpty()) {
            stream = stream.filter(l -> l.getAction().toLowerCase().trim().contains(action.toLowerCase().trim()));
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            stream = stream.filter(l -> !l.getTime().toLocalDate().isBefore(start));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            stream = stream.filter(o -> !o.getTime().toLocalDate().isAfter(end));
        }

        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("manager", manager);
        model.addAttribute("manageLogList", stream.toList().reversed());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("action", action);
        model.addAttribute("managerName",  managerName);
        return "manageLogPage";
    }

    @GetMapping("/category")
    public String category(@RequestParam(required = false, name = "name") String categoryName,
                           @RequestParam(required = false) String status,
                           Model model, HttpSession session)
    {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        Stream<Categories> stream = categoryService.getAllCategories().stream();
        if(categoryName != null &&  !categoryName.isEmpty()){
            stream = stream.filter( c -> c.getName().toLowerCase().contains(categoryName.toLowerCase().trim()));
            model.addAttribute("categoryName", categoryName);
        }
        if(status!=null&&!status.isEmpty()){
            stream=stream.filter(c -> c.getStatus().equals(status));
            model.addAttribute("status",status);
        }
        model.addAttribute("categoryList", stream.toList());
        model.addAttribute("manager", manager);
        model.addAttribute("newCategory", new CategoryDTO());
        return "manageCategory";
    }

    @PostMapping("/addCategory")
    public String addCategory(@Valid @ModelAttribute("newCategory") CategoryDTO request, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session, Model model)
    {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(bindingResult.hasErrors()) {
            model.addAttribute("categoryList", categoryService.getAllCategories());
            model.addAttribute("manager", manager);
            return "manageCategory";
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "add category "+ request.getName());
        managerLogService.save(log);
        Categories category = new Categories(request.getName(), request.getDescription());
        categoryService.save(category);
        return "redirect:/manage/category";
    }

    @GetMapping("/statusCategory/{id}")
    public String updateStatusCategory(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session) {
        Categories category = categoryService.getCategoryById(id);
        Stream<Product> stream = productService.getAllProduct().stream();
        stream = stream.filter(p -> p.getCategory().getId() == category.getId());
        List<Product> productList =  stream.toList();
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        if(category.getStatus().equals("available")) {
            category.setStatus("unavailable");
            productList.forEach(p -> p.setStatus("unavailable"));
            redirectAttributes.addFlashAttribute("msg", "Đã tắt khả năng thêm sản phẩm cho danh mục này");
        }else{
            category.setStatus("available");
            productList.forEach(p -> p.setStatus("available"));
            redirectAttributes.addFlashAttribute("msg", "Đã bật khả năng thêm sản phẩm cho danh mục này");
        }
        ManagerLog log =  new ManagerLog(manager.getUsername(), "change status of category "+ category.getName());
        managerLogService.save(log);
        productService.saveAll(productList);
        categoryService.save(category);
        return "redirect:/manage/category";
    }

    @GetMapping("/updateCategory/{id}")
    public String updateCategory(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        Manager manager=(Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        Categories category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        model.addAttribute("manager", manager);
        model.addAttribute("request", new CategoryDTO(category.getId(), category.getName(), category.getDescription()));
        return "categoryUpdatePage";
    }

    @PostMapping("updateCategory")
    public String updateVariant(@ModelAttribute("request") CategoryDTO request, HttpSession session, RedirectAttributes redirectAttributes) {
        Categories category =  categoryService.getCategoryById(request.getCategory_id());
        Manager manager=(Manager)session.getAttribute("manager");
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryService.save(category);
        ManagerLog log =  new ManagerLog(manager.getUsername(), "update category "+ request.getName());
        managerLogService.save(log);
        return "redirect:/manage/category";
    }

}


