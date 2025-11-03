package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.ReviewRepository;
import anbd.he191271.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/buydetail")
@Controller
public class BuydetailController {
    @Autowired
    private ReviewRepository reviewRepository;
    private final ProductService productService;

    public BuydetailController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public String productBuyDetail(
            @PathVariable("id") int id,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            Model model
    ) {
        Product product = productService.findProductById(id);
        List<Variant> variants = product.getVariants().stream()
                .filter(variant -> variant.getStatus().equals("available"))
                .toList();

        // Tìm variant được chọn
        Variant selectedVariant = null;
        if (variantId != null) {
            selectedVariant = variants.stream()
                    .filter(v -> v.getId() == variantId)
                    .findFirst()
                    .orElse(null);
        }

        // Nếu không có variant được chọn, chọn variant đầu tiên
        if (selectedVariant == null && !variants.isEmpty()) {
            selectedVariant = variants.get(0);
        }
        List<Review> reviews = reviewRepository.findByProduct_Id(id);
        double averageRating = reviews.isEmpty()
                ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("selectedVariant", selectedVariant);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("totalReviews", reviews.size());

        return "buydetail";
    }

    @PostMapping("/{id}/calculate-price")
    @ResponseBody
    public Map<String, Object> calculatePrice(
            @PathVariable("id") int productId,
            @RequestParam("variantId") int variantId,
            @RequestParam("quantity") int quantity) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy thông tin variant
            Variant variant = productService.findVariantById(variantId);
            if (variant == null) {
                response.put("success", false);
                response.put("message", "Variant không tồn tại");
                return response;
            }

            // Kiểm tra xem variant có thuộc về product không
            if (variant.getProduct().getId() != productId) {
                response.put("success", false);
                response.put("message", "Variant không thuộc về sản phẩm này");
                return response;
            }

            // Tính tổng giá (chưa bao gồm voucher)
            double unitPrice = variant.getPrice();
            double totalPrice = unitPrice * quantity;
            double discount = 0;
            double finalPrice = totalPrice;

            // Format giá để hiển thị
            response.put("success", true);
            response.put("unitPrice", unitPrice);
            response.put("totalPrice", totalPrice);
            response.put("discount", discount);
            response.put("finalPrice", finalPrice);
            response.put("quantity", quantity);
            response.put("formattedUnitPrice", formatPrice(unitPrice));
            response.put("formattedTotalPrice", formatPrice(totalPrice));
            response.put("formattedDiscount", formatPrice(discount));
            response.put("formattedFinalPrice", formatPrice(finalPrice));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tính giá: " + e.getMessage());
        }

        return response;
    }

    private String formatPrice(double price) {
        return String.format("%,.0f đ", price);
    }
}