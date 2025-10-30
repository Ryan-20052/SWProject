// File: src/main/java/anbd/he191271/controller/CartController.java
package anbd.he191271.controller;

import anbd.he191271.dto.CartItemDTO;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.ShoppingCart;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.ShoppingCartRepository;
import anbd.he191271.repository.VariantRepository;
import anbd.he191271.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private CartService cartService;

    /**
     * Thêm sản phẩm vào giỏ hàng (nếu đã có cùng variant sẽ cộng dồn số lượng)
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam("variantId") Long variantId,
            @RequestParam(value = "amount", required = false) Integer amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            redirectAttributes.addAttribute("message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            return "redirect:/login.html";
        }

        Variant variant = variantRepository.findById(variantId.intValue()).orElse(null);
        if (variant == null) {
            redirectAttributes.addAttribute("message", "Không tìm thấy phiên bản sản phẩm!");
            return "redirect:/home/hompage";
        }

        // Nếu đã tồn tại cart item cùng customer + variant -> update amount
        Optional<ShoppingCart> existing = shoppingCartRepository.findByCustomerIdAndVariant_Id((long) customer.getId(), variantId);
        if (existing.isPresent()) {
            ShoppingCart sc = existing.get();
            sc.setAmount(sc.getAmount() + (amount == null ? 1 : amount));
            shoppingCartRepository.save(sc);
        } else {
            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setCustomerId((long) customer.getId());
            cartItem.setVariant(variant);
            cartItem.setAmount(amount == null ? 1 : amount);
            shoppingCartRepository.save(cartItem);
        }

        redirectAttributes.addFlashAttribute("message", "Thêm vào giỏ hàng thành công!");
        Long productId = (long) variant.getProduct().getId();
        return "redirect:/product/" + productId;
    }



    @PostMapping("/update")
    public String updateCart(@RequestParam("itemIds") List<Long> itemIds,
                             @RequestParam("quantities") List<Integer> quantities,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        for (int i = 0; i < itemIds.size(); i++) {
            Long id = itemIds.get(i);
            Integer qty = quantities.get(i);

            ShoppingCart cartItem = shoppingCartRepository.findById(id).orElse(null);
            if (cartItem != null && cartItem.getCustomerId().equals((long) customer.getId())) {
                cartItem.setAmount(qty);
                shoppingCartRepository.save(cartItem);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Cập nhật giỏ hàng thành công!");
        return "redirect:/cart/view";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("itemId") Long itemId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        ShoppingCart item = shoppingCartRepository.findById(itemId).orElse(null);
        if (item != null && item.getCustomerId().equals((long) customer.getId())) {
            shoppingCartRepository.delete(item);
            redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng!");
        }

        return "redirect:/cart/view";
    }

    // SỬA METHOD VIEWCART - ĐỔI TÊN PARAMETERS
    @GetMapping("/view")
    public String viewCart(@RequestParam(required = false) String category,
                           @RequestParam(required = false) Integer minSubtotal,  // ĐỔI TÊN
                           @RequestParam(required = false) Integer maxSubtotal,  // ĐỔI TÊN
                           @RequestParam(required = false) String duration,
                           Model model, HttpSession session) {

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        // Lấy items với filter
        List<CartItemDTO> items = cartService.getFilteredCartItems(
                customer.getId(), category, minSubtotal, maxSubtotal, duration);

        // Tính tổng tiền
        long totalPrice = cartService.computeTotal(items);

        // Lấy danh sách filter options
        List<String> categories = cartService.getAvailableCategories(customer.getId());
        List<String> durations = cartService.getAvailableDurations(customer.getId());

        // Kiểm tra có filter active không
        boolean hasFilters = cartService.hasActiveFilters(category, minSubtotal, maxSubtotal, duration);

        // Add attributes to model
        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("categories", categories);
        model.addAttribute("durations", durations);
        model.addAttribute("hasFilters", hasFilters);
        model.addAttribute("category", category);
        model.addAttribute("minSubtotal", minSubtotal);  // ĐỔI TÊN
        model.addAttribute("maxSubtotal", maxSubtotal);  // ĐỔI TÊN
        model.addAttribute("duration", duration);

        return "shoppingCart";
    }

}