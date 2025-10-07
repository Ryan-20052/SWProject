package anbd.he191271.dto;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;

public class CartItemDTO {
    private Long id;
    private Product product;
    private Variant variant;
    private int quantity;
    private long subtotal; // price * quantity

    // constructors, getters, setters
    public CartItemDTO() {}

    public CartItemDTO(Long id, Product product, Variant variant, int quantity, long subtotal) {
        this.id = id;
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
}
