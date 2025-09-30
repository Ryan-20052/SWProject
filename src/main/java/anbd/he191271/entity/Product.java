package anbd.he191271.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GeneratedColumn;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String name;

    @Column(name = "manager_id")
    private int managerId;
    @Column(name = "img_url")
    private String imgUrl;

    @ManyToOne
    @JoinColumn(name = "categories_id", nullable = false)
    private Categories category;

    public Product() {
    }

    public Product(int id, String name, Categories category, int managerId, String imgUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.managerId = managerId;
        this.imgUrl = imgUrl;
    }

    public Product(String name, int managerId, String imgUrl, Categories category) {
        this.name = name;
        this.managerId = managerId;
        this.imgUrl = imgUrl;
        this.category = category;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Categories getCategory() {
        return category;
    }

    public void setCategory(Categories category) {
        this.category = category;
    }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }
   public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
}
