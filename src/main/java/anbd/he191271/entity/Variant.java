package anbd.he191271.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "variant")
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String name;

    @Column(name = "stock_quantity")
    private int stock_quantity;
    @Column(name = "duration")
    private String duration;
    @Column(name = "price")
    private int price;

    // Quan hệ với product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Variant() {
    }

    public Variant(int id, String name, int stock_quantity, String duration, int price, Product product) {
        this.id = id;
        this.name = name;
        this.stock_quantity = stock_quantity;
        this.duration = duration;
        this.price = price;
        this.product = product;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(int stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
