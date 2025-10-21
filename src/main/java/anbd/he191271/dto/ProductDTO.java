package anbd.he191271.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductDTO {
    private Integer productId;
    @NotBlank(message = "Không được để trống")
    private String name;
    private Integer categoryId;
    @NotBlank(message = "Không được để trống")
    private String imgUrl;
    @NotBlank(message = "Không được để trống")
    private String description;

    public ProductDTO() {
    }

    public ProductDTO(String name, Integer categoryId, String imgUrl, String description) {
        this.name = name;
        this.categoryId = categoryId;
        this.imgUrl = imgUrl;
        this.description = description;
    }

    public ProductDTO(Integer productId, String name, Integer categoryId, String imgUrl , String description) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.imgUrl = imgUrl;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
