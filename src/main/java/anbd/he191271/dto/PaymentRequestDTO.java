package anbd.he191271.dto;

public class PaymentRequestDTO {
    private long amount;
    private String orderInfo;
    private String orderType;
    private String locale;

    // getter & setter
    public long getAmount() {
        return amount;
    }
    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getOrderType() {
        return orderType;
    }
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
