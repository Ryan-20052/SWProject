package anbd.he191271.dto;

import lombok.Data;

import java.util.List;

// Thay cho PaymentRequestDTO cũ
public class PaymentRequestDTO {
    private List<Item> items; // giỏ hàng
    private String orderInfo; // mô tả đơn
    private String orderType; // optional
    private String locale;    // optional (vn/en)
    private Integer customerId;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Data
    public static class Item {
        private int variantId;
        private Integer quantity;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
