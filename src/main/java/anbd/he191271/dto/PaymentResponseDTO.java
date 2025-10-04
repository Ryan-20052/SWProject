package anbd.he191271.dto;

public class PaymentResponseDTO {
    private String code;       // Mã kết quả
    private String message;    // Thông điệp
    private String paymentUrl; // URL thanh toán (nếu có)

    private Integer orderId;
    private String orderCode;
    private Long totalAmount;

    private String customerName;
    private String customerEmail;

    public PaymentResponseDTO(String code, String message, String paymentUrl, Integer orderId, String orderCode, Long totalAmount, String customerName, String customerEmail) {
        this.code = code;
        this.message = message;
        this.paymentUrl = paymentUrl;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }





    // getter & setter
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
