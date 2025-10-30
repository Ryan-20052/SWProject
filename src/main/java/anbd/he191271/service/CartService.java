package anbd.he191271.service;

import anbd.he191271.dto.CartItemDTO;
import anbd.he191271.entity.ShoppingCart;
import anbd.he191271.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // THÊM METHOD LỌC ĐƠN GIẢN
    // THÊM METHOD LỌC THEO SUBTOTAL (giá × số lượng)
    public List<CartItemDTO> getFilteredCartItems(Integer customerId, String category,
                                                  Integer minSubtotal, Integer maxSubtotal,
                                                  String duration) {
        List<CartItemDTO> allItems = getCartItemsByCustomer(customerId);

        // Nếu không có filter, trả về tất cả
        if ((category == null || category.isEmpty()) &&
                (minSubtotal == null) &&
                (maxSubtotal == null) &&
                (duration == null || duration.isEmpty())) {
            return allItems;
        }

        // Lọc đơn giản trong memory
        return allItems.stream()
                .filter(item -> {
                    boolean match = true;

                    // Lọc theo category
                    if (category != null && !category.isEmpty()) {
                        if (item.getProduct().getCategory() != null) {
                            String itemCategory = item.getProduct().getCategory().getName();
                            match = match && itemCategory.equals(category);
                        } else {
                            match = false;
                        }
                    }

                    // Lọc theo SUBTOTAL (giá × số lượng) - SỬA Ở ĐÂY
                    if (minSubtotal != null) {
                        match = match && (item.getSubtotal() >= minSubtotal);
                    }
                    if (maxSubtotal != null) {
                        match = match && (item.getSubtotal() <= maxSubtotal);
                    }

                    // Lọc theo duration
                    if (duration != null && !duration.isEmpty()) {
                        if (item.getVariant().getDuration() != null) {
                            match = match && item.getVariant().getDuration().equals(duration);
                        } else {
                            match = false;
                        }
                    }

                    return match;
                })
                .collect(Collectors.toList());
    }

    // Kiểm tra có filter đang active không - SỬA PARAMETER NAMES
    public boolean hasActiveFilters(String category, Integer minSubtotal, Integer maxSubtotal, String duration) {
        return (category != null && !category.isEmpty()) ||
                (minSubtotal != null) ||
                (maxSubtotal != null) ||
                (duration != null && !duration.isEmpty());
    }

    // Lấy danh sách categories có trong giỏ hàng
    public List<String> getAvailableCategories(Integer customerId) {
        List<CartItemDTO> items = getCartItemsByCustomer(customerId);
        return items.stream()
                .filter(item -> item.getProduct().getCategory() != null)
                .map(item -> item.getProduct().getCategory().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    // Lấy danh sách durations có trong giỏ hàng
    public List<String> getAvailableDurations(Integer customerId) {
        List<CartItemDTO> items = getCartItemsByCustomer(customerId);
        return items.stream()
                .filter(item -> item.getVariant().getDuration() != null)
                .map(item -> item.getVariant().getDuration())
                .distinct()
                .collect(Collectors.toList());
    }



    public long computeTotal(List<CartItemDTO> cartItems) {
        return cartItems.stream().mapToLong(CartItemDTO::getSubtotal).sum();
    }

    public List<CartItemDTO> getCartItemsByCustomer(long id) {
        return getCartItemsForCustomer(id);
    }
}