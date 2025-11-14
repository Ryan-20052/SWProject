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

    // ✅ Hiển thị danh sách review có lọc, phân trang và sắp xếp
    @GetMapping("/review/list")
    public String viewReviews(
            @RequestParam("productId") int productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model
    ) {
        try {
            // ✅ VALIDATE DATE RANGE
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                if (end.isBefore(start)) {
                    model.addAttribute("errorMessage", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
                    // Vẫn tiếp tục xử lý nhưng không áp dụng filter date
                    startDate = null;
                    endDate = null;
                }
            }

            // SỬA: Sử dụng LocalDate thay vì Date
            LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
            LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

            // Lấy danh sách reviews với sắp xếp
            Page<Review> reviews = reviewService.getFilteredReviews(productId, rating, hasImage, start, end, page, size, sort);

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
            model.addAttribute("selectedSort", sort);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại!");
        }

        return "viewReview";
    }

    // ✅ Hiển thị form review - ĐÃ THÊM VALIDATION MUA HÀNG
    @GetMapping("/review/{productId}")
    public String showReviewForm(@PathVariable int productId, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            model.addAttribute("errorMessage", "Sản phẩm không tồn tại!");
            return "redirect:/purchasedlicenses";
        }

        boolean hasPurchased = reviewService.hasCustomerPurchasedProduct(customer.getId(), productId);
        if (!hasPurchased) {
            model.addAttribute("errorMessage",
                    "Bạn cần mua sản phẩm này trước khi đánh giá! " +
                            "Vui lòng kiểm tra trong danh sách license đã mua.");
            return "redirect:/purchasedlicenses";
        }

        Optional<Review> existingReview = reviewRepository.findByCustomer_IdAndProduct_Id(customer.getId(), productId);
        Review review = existingReview.orElse(new Review());
        review.setProduct(product);

        model.addAttribute("product", product);
        model.addAttribute("review", review);
        return "review";
    }

    @PostMapping("/review/save")
    public String saveReview(@ModelAttribute Review review,
                             @RequestParam("productId") int productId,
                             @RequestParam(value = "rating", required = false) Integer rating,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session,
                             Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            model.addAttribute("errorMessage", "Sản phẩm không tồn tại!");
            return "redirect:/purchasedlicenses";
        }

        boolean hasPurchased = reviewService.hasCustomerPurchasedProduct(customer.getId(), productId);
        if (!hasPurchased) {
            model.addAttribute("errorMessage",
                    "Bạn cần mua sản phẩm này trước khi đánh giá! " +
                            "Hành động này đã được ghi nhận.");
            return "redirect:/purchasedlicenses";
        }

        Optional<Review> existingReview = reviewRepository.findByCustomer_IdAndProduct_Id(customer.getId(), productId);

        // ===== VALIDATION =====
        String comment = review.getComment();
        if (comment != null) {
            comment = comment.trim();
            if (comment.isEmpty()) comment = null;
        }
        review.setComment(comment);

        // 🟡 VALIDATE RATING
        if (rating == null || rating < 1 || rating > 5) {
            model.addAttribute("errorMessage", "Vui lòng chọn số sao đánh giá từ 1 đến 5!");
            model.addAttribute("product", product);
            model.addAttribute("review", review);
            return "review";
        }
        // Gán rating hợp lệ
        review.setRating(rating);

        // ✅ VALIDATE FILE SIZE VÀ TYPE
        if (imageFile != null && !imageFile.isEmpty()) {
            // Kiểm tra kích thước file (tối đa 5MB)
            if (imageFile.getSize() > 5 * 1024 * 1024) {
                model.addAttribute("errorMessage", "Kích thước ảnh không được vượt quá 5MB!");
                model.addAttribute("product", product);
                model.addAttribute("review", review);
                return "review";
            }

            // Kiểm tra loại file
            String contentType = imageFile.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpg"))) {
                model.addAttribute("errorMessage", "Chỉ chấp nhận file ảnh định dạng JPG, JPEG hoặc PNG!");
                model.addAttribute("product", product);
                model.addAttribute("review", review);
                return "review";
            }
        }

        try {
            byte[] imageBytes = (imageFile != null && !imageFile.isEmpty()) ? imageFile.getBytes() : null;

            Review r = existingReview.orElseGet(Review::new);
            r.setCustomer(customer);
            r.setProduct(product);
            r.setRating(review.getRating());
            r.setComment(comment);
            r.setCreatedAt(LocalDateTime.now());

            // ✅ Xử lý ảnh
            if (imageBytes != null) {
                r.setReviewImage(imageBytes);
                r.setHasImage(true);
            } else {
                if (existingReview.isPresent() && existingReview.get().getHasImage()) {
                    r.setReviewImage(existingReview.get().getReviewImage());
                    r.setHasImage(true);
                } else {
                    r.setReviewImage(null);
                    r.setHasImage(false);
                }
            }

            System.out.println("🟡 Saving review - Rating: " + r.getRating() + ", HasImage: " + r.getHasImage());
            reviewRepository.save(r);
            System.out.println("✅ Review saved successfully! ID: " + r.getId());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi lưu đánh giá. Vui lòng thử lại!");
            model.addAttribute("product", product);
            model.addAttribute("review", review);
            return "review";
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