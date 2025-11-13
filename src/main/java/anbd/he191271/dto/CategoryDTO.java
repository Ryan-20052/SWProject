package anbd.he191271.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class CategoryDTO {
    private Integer  category_id;
    @NotBlank(message = "Không được để trống")
    @Length(max = 250, message = "Không quá 250 kí tự")
    private String name;
    @NotBlank(message = "Không được để trống")
    @Length(max = 250, message = "Không quá 250 kí tự")
    private String description;

    public CategoryDTO() {
    }

    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CategoryDTO(Integer category_id, String name, String description) {
        this.category_id = category_id;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }
}
