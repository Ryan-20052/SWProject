package anbd.he191271.entity;

import java.util.Date;

public class LicenseKey {
    private int id;
    private String key;
    private int order_detail_id;
    private Date activated_at;
    private Date expired_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getOrder_detail_id() {
        return order_detail_id;
    }

    public void setOrder_detail_id(int order_detail_id) {
        this.order_detail_id = order_detail_id;
    }

    public Date getActivated_at() {
        return activated_at;
    }

    public void setActivated_at(Date activated_at) {
        this.activated_at = activated_at;
    }

    public Date getExpired_at() {
        return expired_at;
    }

    public void setExpired_at(Date expired_at) {
        this.expired_at = expired_at;
    }
}
