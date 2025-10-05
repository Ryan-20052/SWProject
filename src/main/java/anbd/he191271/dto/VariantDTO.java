package anbd.he191271.dto;

public class VariantDTO {
    private Integer id;
    private String name;
    private String duration;
    private Integer price;
    private Integer productId;

    public VariantDTO() {
    }

    public VariantDTO(Integer id, String name, String duration, Integer price, Integer productId) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.price = price;
        this.productId = productId;
    }

    // getter & setter


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}

