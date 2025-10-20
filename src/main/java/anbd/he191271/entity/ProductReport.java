package anbd.he191271.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "productreport")
public class ProductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String title;
    private String email;

    private String message;

    private String type;




    @Column(name = "customer_id")
    private int customer_id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "manager_msg")
    private String managerMsg;

    private String status; // ví dụ: "pending", "approved", "rejected"

    // Constructors
    public ProductReport() {}

    public ProductReport(Long id, String name, String title, String email, String message, String type, int customer_id, Long productId, LocalDateTime createdAt, LocalDateTime updatedAt, String managerMsg, String status) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.email = email;
        this.message = message;
        this.type = type;
        this.customer_id = customer_id;
        this.productId = productId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.managerMsg = managerMsg;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getManagerMsg() {
        return managerMsg;
    }
    public void setManagerMsg(String description) {
        this.managerMsg = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}