package anbd.he191271.dto;

import java.util.List;

public class ProductDTO {
    private int id;
    private String name;
    private List<VariantDTO> variants;

    // Constructor
    public ProductDTO(int id, String name, List<VariantDTO> variants) {
        this.id = id;
        this.name = name;
        this.variants = variants;
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
}
