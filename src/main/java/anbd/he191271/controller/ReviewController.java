package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.ReviewRepository;
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

import java.util.Date;
import java.util.Optional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository<Product, Integer> productRepository; // ✅ chỉnh lại cho khớp generic

    // Hiển thị form review
    @GetMapping("/review/{productId}")
    public String showReviewForm(@PathVariable int productId, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/purchasedlicenses";
        }

        // ✅ lấy lại review trong DB (nếu có)
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
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/purchasedlicenses";
        }

        Optional<Review> existingReview =
                reviewRepository.findByCustomer_IdAndProduct_Id(customer.getId(), productId);

        try {
            byte[] imageBytes = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageBytes = imageFile.getBytes();
            }

            if (existingReview.isPresent()) {
                Review old = existingReview.get();
                old.setRating(review.getRating());
                old.setComment(review.getComment());
                if (imageBytes != null) old.setReviewImage(imageBytes);
                old.setCreatedAt(new Date());
                reviewRepository.save(old);
            } else {
                review.setCustomer(customer);
                review.setProduct(product);
                review.setCreatedAt(new Date());
                if (imageBytes != null) review.setReviewImage(imageBytes);
                reviewRepository.save(review);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
