package anbd.he191271.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "manager_id")
    private int manager_id;

    @Column(name = "img_url")
    private String img_url;

    @Column(name = "status")
    private  String status;

    @ManyToOne
    @JoinColumn(name = "categories_id", nullable = false)
    private Categories category;

    @Column(name="description")
    private String description;


    // <-- Thêm trường variants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Variant> variants = new ArrayList<>();


    public Product() {}

    public Product(String name, int managerId, String imgUrl, Categories category,  String description) {
        this.name = name;
        this.manager_id = managerId;
        this.img_url = imgUrl;
        this.category = category;
        this.description = description;
        this.status = "available";
    }

    // constructor, getters và setters (đảm bảo có getter/setter cho variants)
    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }



    public Product(int id, String name, int manager_id, String img_url, String status, Categories category, String description, List<Variant> variants) {
        this.id = id;
        this.name = name;
        this.manager_id = manager_id;
        this.img_url = img_url;
        this.status = status;
        this.category = category;
        this.description = description;
        this.variants = variants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Categories getCategory() {
        return category;
    }

    public void setCategory(Categories category) {
        this.category = category;
    }

    public int getManager_id() {
        return manager_id;
    }

    public void setManager_id(int manager_id) {
        this.manager_id = manager_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
