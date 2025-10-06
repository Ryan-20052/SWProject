package anbd.he191271.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "shopping_cart",
        indexes = {
                @Index(name = "idx_shoppingcart_customer", columnList = "customer_id"),
                @Index(name = "idx_shoppingcart_variant", columnList = "variant_id")
        }
)
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // map tới variant (sử dụng cột variant_id hiện có trong DB)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(name = "amount", nullable = false)
    private Integer amount = 1;

    public ShoppingCart() {}

    public ShoppingCart(Long customerId, Variant variant, Integer amount) {
        this.customerId = customerId;
        this.variant = variant;
        this.amount = amount == null ? 1 : amount;
    }

    // getters / setters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) {
        if (amount == null || amount < 1) throw new IllegalArgumentException("amount must be >= 1");
        this.amount = amount;
    }


    // equals, hashCode, toString giống trước
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", variant=" + (variant != null ? variant.getId() : null) +
                ", amount=" + amount +
                '}';
    }
}
