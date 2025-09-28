package anbd.he191271.dto;

import java.util.List;

public class ProductDTO {
    private int id;
    private String name;
    private List<VariantDTO> variants;
    private String imgUrl;

    // Constructor

    public ProductDTO() {
    }

    public ProductDTO(int id, String name, List<VariantDTO> variants, String imgUrl) {
        this.id = id;
        this.name = name;
        this.variants = variants;
        this.imgUrl = imgUrl;
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

    public List<VariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantDTO> variants) {
        this.variants = variants;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
