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

    @GetMapping("/view")
    public String viewCart(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }
        List<CartItemDTO> items = cartService.getCartItemsByCustomer(customer.getId());
        model.addAttribute("cartItems", items);
        return "shoppingCart"; // → file cart.html
    }
    @PostMapping("/checkout")
    public String checkout(@RequestParam(value = "selectedItems", required = false) List<Long> selectedItems,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }

        if (selectedItems == null || selectedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
            return "redirect:/cart/view";
        }

        // Lấy danh sách sản phẩm theo ID được chọn
        List<ShoppingCart> items = shoppingCartRepository.findAllById(selectedItems);

        // 👉 TODO: Xử lý thanh toán / tạo đơn hàng ở đây (sẽ thêm ở bước sau)
        // Sau khi thanh toán thành công:
        shoppingCartRepository.deleteAll(items);

        redirectAttributes.addFlashAttribute("message", "Thanh toán thành công! Các sản phẩm đã được xoá khỏi giỏ hàng.");
        return "redirect:/cart/view";
    }

}