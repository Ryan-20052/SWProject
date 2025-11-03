package anbd.he191271.dto;

import jakarta.validation.constraints.*;

public class VariantDTO {
    private Integer id;
    @NotBlank(message = "Không được để trống")
    private String name;
    @NotBlank(message = "Thời hạn không được để trống")
    @Pattern(
            regexp = "^\\d+\\s(ngày|tháng|năm)$",
            message = "Thời hạn phải là số kèm đơn vị: ngày, tháng hoặc năm (VD: '30 ngày', '3 tháng', '1 năm')"
    )
    private String duration;
    @NotNull(message = "Giá không được để trống")
    @Min(value = 1000, message = "Giá phải lớn hơn hoặc bằng 1.000 VNĐ")
    @Max(value = 100000000, message = "Giá không được vượt quá 100.000.000 VNĐ")
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

