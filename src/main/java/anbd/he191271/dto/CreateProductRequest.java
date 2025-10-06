package anbd.he191271.dto;

public class CreateProductRequest {
    private Integer productId;
    private String name;
    private Integer categoryId;
    private String imgUrl;

    public CreateProductRequest() {
    }

    public CreateProductRequest(String name, Integer categoryId, String imgUrl) {
        this.name = name;
        this.categoryId = categoryId;
        this.imgUrl = imgUrl;
    }

    public CreateProductRequest(Integer productId, String name, Integer categoryId, String imgUrl) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.imgUrl = imgUrl;
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
