package anbd.he191271.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khách hàng
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Nếu muốn giữ lại order để biết review phát sinh từ order nào → cho phép NULL
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    // Product là bắt buộc
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Variant thì không cần nữa, cho phép NULL
    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = true)
    private Variant variant;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", length = 255)
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Lob
    @Column(name = "review_image", columnDefinition = "LONGBLOB")
    private byte[] reviewImage;

    public byte[] getReviewImage() {
        return reviewImage;
    }

    public void setReviewImage(byte[] reviewImage) {
        this.reviewImage = reviewImage;
    }

    // ===== Getters & Setters =====
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

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
