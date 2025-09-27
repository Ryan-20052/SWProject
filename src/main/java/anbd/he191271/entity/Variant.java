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
    private int stockQuantity;
    @Column(name = "duration")
    private String duration;
    @Column(name = "price")
    private int price;

    // Quan hệ với product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
