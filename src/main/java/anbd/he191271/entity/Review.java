package anbd.he191271.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Lob
    @Column(name = "review_image")
    private byte[] reviewImage;

    @Column(name = "has_image")
    private Boolean hasImage = false;

    // ===== Constructors =====
    public Review() {
        this.createdAt = new Date();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    // ===== Getter & Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) {
        if (rating != null && rating >= 1 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public byte[] getReviewImage() { return reviewImage; }

    public void setReviewImage(byte[] reviewImage) {
        this.reviewImage = reviewImage;
        this.hasImage = (reviewImage != null && reviewImage.length > 0);
    }

    public Boolean getHasImage() { return hasImage; }
    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage != null ? hasImage : false;
    }

    // ===== Utility Methods =====
    public boolean hasImage() {
        return Boolean.TRUE.equals(hasImage);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getId() : "null") +
                ", product=" + (product != null ? product.getId() : "null") +
                ", rating=" + rating +
                ", hasImage=" + hasImage +
                '}';
    }
}