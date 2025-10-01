package anbd.he191271.dto;

public class PaymentResponseDTO {
    private String code;         // 00: success, khác: lỗi
    private String message;
    private String paymentUrl;   // URL VNPAY trả về để redirect

    public PaymentResponseDTO(String code, String message, String paymentUrl) {
        this.code = code;
        this.message = message;
        this.paymentUrl = paymentUrl;
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
