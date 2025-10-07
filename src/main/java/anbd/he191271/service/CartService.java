// File: src/main/java/anbd/he191271/service/CartService.java
package anbd.he191271.service;

import anbd.he191271.dto.CartItemDTO;
import anbd.he191271.entity.ShoppingCart;
import anbd.he191271.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    /**
     * Lấy danh sách CartItemDTO cho customerId (JOIN FETCH đã được định nghĩa trong repository)
     */
    public List<CartItemDTO> getCartItemsForCustomer(Long customerId) {
        List<ShoppingCart> items = shoppingCartRepository.findByCustomerIdWithVariantAndProduct(customerId);
        List<CartItemDTO> dtos = new ArrayList<>();
        for (ShoppingCart sc : items) {
            if (sc.getVariant() == null || sc.getVariant().getProduct() == null) continue;
            long price = sc.getVariant().getPrice();
            int qty = sc.getAmount();
            long subtotal = price * (long) qty;
            dtos.add(new CartItemDTO(sc.getId(), sc.getVariant().getProduct(), sc.getVariant(), qty, subtotal));
        }
        return dtos;
    }

    public long computeTotal(List<CartItemDTO> cartItems) {
        return cartItems.stream().mapToLong(CartItemDTO::getSubtotal).sum();
    }

    public List<CartItemDTO> getCartItemsByCustomer(long id) {
        return getCartItemsForCustomer(id);
    }
}