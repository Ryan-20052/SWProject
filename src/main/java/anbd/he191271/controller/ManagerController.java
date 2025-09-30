package anbd.he191271.controller;

import anbd.he191271.dto.CreateProductRequest;
import anbd.he191271.entity.Categories;
import anbd.he191271.entity.Product;
import anbd.he191271.service.CategoryService;
import anbd.he191271.service.ProductService;
import anbd.he191271.service.VariantService;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//@RestController
//@RequestMapping("/manage")
//public class ManagerController {
//    private final VariantService variantService;
//
//    public ManagerController(VariantService variantService) {
//        this.variantService = variantService;
//    }
//@GetMapping("/loadVariant")
//    public List<Variant> LoadVariant(){
//        return variantService.getAllVariant();
//    }
////    @PostMapping("/editVariant")
////    public Void  editVariant(@RequestBody Variant variant){
////
////    }
//
//    @DeleteMapping("/deleteVariant/{id}")
//    public String  deleteVariant(@PathVariable int id){
//        variantService.deleteVariant(id);
//        return "Đã xóa thành công";
//    }
//
//    @GetMapping("/getVariant/{id}")
//    public Variant getVariant(@PathVariable int id){
//        return variantService.getVariantById(id);
//    }
//}

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
    public String manageHome(Model model) {
        model.addAttribute("productList", productService.getAllProduct());
        model.addAttribute("newProduct", new CreateProductRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manageHome";
    }

    @PostMapping("/addProduct")
    public String addProduct(@ModelAttribute("newProduct") CreateProductRequest request) {
        Categories category = categoryService.getCategoryById(request.getCategoryId());
        Product product=new Product(request.getName(),request.getManagerId(),request.getImgUrl(),category);
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("newProduct") Product product) {
        productService.saveProduct(product);
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/deleteVariant/{id}")
    public String deleteVariant(@PathVariable int id, RedirectAttributes redirectAttributes) {
        variantService.deleteVariant(id);
        redirectAttributes.addFlashAttribute("msg", "Đã xóa thành công");
        return "redirect:/manage/manageHome";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("msg", "Đã xóa thành công");
        return "redirect:/manage/manageHome";
    }
}


