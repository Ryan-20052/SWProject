package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Review;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

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

        if (existingReview.isPresent()) {
            Review old = existingReview.get();
            old.setRating(review.getRating());
            old.setComment(review.getComment());
            old.setCreatedAt(new Date());
            reviewRepository.save(old);
        } else {
            review.setCustomer(customer);
            review.setProduct(product);
            review.setCreatedAt(new Date());
            reviewRepository.save(review);
        }

        return "redirect:/purchasedlicenses";
    }
}

