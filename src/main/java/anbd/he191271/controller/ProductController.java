package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.repository.ReviewRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/product/{id}")
    public String productDetail(
            @PathVariable("id") int id,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            Model model) {

        Product product = productService.findProductById(id);
        if (product == null) {
            // xử lý khi không tìm thấy product — bạn có thể redirect hoặc trả về 404
            return "product";
        }

        List<Variant> variants = product.getVariants()
                .stream()
                .filter(v -> "available".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());


        Variant selectedVariant = null;
        if (variantId != null && variants != null) {
            for (Variant v : variants) {
                // DÙNG Objects.equals để an toàn với null và với cả int (autoboxed)
                if (Objects.equals(variantId, v.getId())) {
                    selectedVariant = v;
                    break;
                }
            }
        }

        if (selectedVariant == null && variants != null && !variants.isEmpty()) {
            selectedVariant = variants.get(0);
        }

        // 👉 Lấy danh sách review theo product
        List<Review> reviews = reviewRepository.findByProduct_Id(id);

        double averageRating = 0;
        if (!reviews.isEmpty()) {
            averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("selectedVariant", selectedVariant);

        // 👉 Gửi thêm dữ liệu review xuống view
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("totalReviews", reviews.size());

        return "product";
    }
}
