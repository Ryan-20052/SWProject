package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.ReviewRepository;
import anbd.he191271.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository<Product, Integer> productRepository;

    @Autowired
    private ReviewService reviewService;

    // ✅ Hiển thị danh sách review có lọc và phân trang
    @GetMapping("/review/list")
    public String viewReviews(
            @RequestParam("productId") int productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            Model model
    ) {
        try {
            // SỬA: Sử dụng LocalDate thay vì Date
            LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
            LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

            // Lấy danh sách reviews (phân trang)
            Page<Review> reviews = reviewService.getFilteredReviews(productId, rating, hasImage, start, end, page, size);

            // Lấy thống kê từ service
            Map<String, Object> stats = reviewService.getReviewStats(productId, rating, hasImage, start, end);

            // Lấy product để truyền sang view
            Product product = productRepository.findById(productId).orElse(null);
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", stats.get("averageRating"));
            model.addAttribute("totalReviews", stats.get("totalReviews"));

            // Các biến hỗ trợ filter/paging trong template
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reviews.getTotalPages());
            model.addAttribute("productId", productId);
            model.addAttribute("selectedRating", rating);
            model.addAttribute("hasImage", hasImage);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "viewReview";
    }

    // ✅ Hiển thị form review
    @GetMapping("/review/{productId}")
    public String showReviewForm(@PathVariable int productId, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/login.html";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/purchasedlicenses";

        Optional<Review> existingReview = reviewRepository.findByCustomer_IdAndProduct_Id(customer.getId(), productId);
        Review review = existingReview.orElse(new Review());
        review.setProduct(product);

        model.addAttribute("product", product);
        model.addAttribute("review", review);
        return "review";
    }

    // ✅ Lưu review - ĐÃ SỬA
    @PostMapping("/review/save")
    public String saveReview(@ModelAttribute Review review,
                             @RequestParam("productId") int productId,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/login.html";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/purchasedlicenses";

        Optional<Review> existingReview = reviewRepository.findByCustomer_IdAndProduct_Id(customer.getId(), productId);

        try {
            byte[] imageBytes = (imageFile != null && !imageFile.isEmpty()) ? imageFile.getBytes() : null;

            Review r = existingReview.orElseGet(Review::new);
            r.setCustomer(customer);
            r.setProduct(product);
            r.setRating(review.getRating());
            r.setComment(review.getComment());
            r.setCreatedAt(LocalDateTime.now());

            // SỬA QUAN TRỌNG: Xử lý ảnh đúng cách
            if (imageBytes != null) {
                r.setReviewImage(imageBytes);
                r.setHasImage(true); // QUAN TRỌNG: Phải set hasImage = true
            } else {
                // Nếu không có ảnh mới upload, nhưng đang edit review có ảnh cũ
                if (existingReview.isPresent() && existingReview.get().getHasImage()) {
                    // Giữ ảnh cũ
                    r.setReviewImage(existingReview.get().getReviewImage());
                    r.setHasImage(true);
                } else {
                    // Không có ảnh
                    r.setReviewImage(null);
                    r.setHasImage(false);
                }
            }

            // THÊM DEBUG
            System.out.println("🟡 Saving review - Rating: " + r.getRating() + ", HasImage: " + r.getHasImage());

            reviewRepository.save(r);

            System.out.println("✅ Review saved successfully! ID: " + r.getId());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error saving review: " + e.getMessage());
        }

        return "redirect:/review/" + productId;
    }

    @GetMapping("/review/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getReviewImage(@PathVariable Long id) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isPresent() && reviewOpt.get().getReviewImage() != null) {
            byte[] imageBytes = reviewOpt.get().getReviewImage();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }
}