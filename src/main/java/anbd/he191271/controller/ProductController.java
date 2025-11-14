package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.VariantRepository;
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
    private VariantRepository variantRepository;
    //Hàm này để lấy variant trong 1 product (Product.html)
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

        // 👉 Chỉ lấy số sao trung bình, không gửi danh sách review xuống
        List<Review> reviews = reviewRepository.findByProduct_Id(id);
        double averageRating = reviews.isEmpty()
                ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("selectedVariant", selectedVariant);

        // Chỉ gửi số sao trung bình & tổng số review
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("totalReviews", reviews.size());

        return "product";
    }

    // ===================== TRANG XEM TẤT CẢ ĐÁNH GIÁ =====================
    @GetMapping("/review/view")
    public String viewReviewPage(@RequestParam("productId") int productId, Model model) {
        Product product = productService.findProductById(productId);
        if (product == null) {
            return "error/404";
        }

        List<Review> reviews = reviewRepository.findByProduct_Id(productId);

        double averageRating = reviews.isEmpty()
                ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("totalReviews", reviews.size());

        return "viewReview"; // trỏ tới viewReview.html
    }
}