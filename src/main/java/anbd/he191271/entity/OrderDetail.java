package anbd.he191271.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "orders_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // N-1: nhiều OrderDetail thuộc về 1 Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // N-1: nhiều OrderDetail thuộc về 1 Variant
    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(name = "amount", nullable = false)
    private int amount;

    // 1-1: 1 OrderDetail có 1 LicenseKey
    @OneToOne(mappedBy = "orderDetail", cascade = CascadeType.ALL)
    private LicenseKey licenseKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public LicenseKey getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(LicenseKey licenseKey) {
        this.licenseKey = licenseKey;
    }
}
