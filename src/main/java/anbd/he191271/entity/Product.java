package anbd.he191271.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GeneratedColumn;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String name;

    @Column(name = "categories_id")
    private int categoryId;

    @Column(name = "manager_id")
    private int managerId;
    @Column(name = "img_url")
    private String imgUrl;

    public Product() {
    }

    public Product(int id, String name, int categoryId, int managerId, String imgUrl) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.managerId = managerId;
        this.imgUrl = imgUrl;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }
   public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
}
