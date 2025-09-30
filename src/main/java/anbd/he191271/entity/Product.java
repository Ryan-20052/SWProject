package anbd.he191271.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "categories_id")
    private int category_id;

    @Column(name = "manager_id")
    private int manager_id;

    @Column(name = "img_url")
    private String img_url;

    // <-- Thêm trường variants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Variant> variants = new ArrayList<>();

    public Product() {}

    // constructor, getters và setters (đảm bảo có getter/setter cho variants)
    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public Product(int id, String name, int category_id, int manager_id, String img_url, List<Variant> variants) {
        this.id = id;
        this.name = name;
        this.category_id = category_id;
        this.manager_id = manager_id;
        this.img_url = img_url;
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

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
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
}
