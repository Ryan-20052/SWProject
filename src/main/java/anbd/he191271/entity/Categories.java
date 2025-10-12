package anbd.he191271.entity;

import jakarta.persistence.*;  // dùng jakarta.persistence cho Spring Boot 3

@Entity
@Table(name = "categories")   // trùng tên bảng trong DB
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // nên dùng wrapper để tránh null pointer

    private String name;

    private String description;

    private String status;

    public Categories() {
    }


    public Categories(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Categories(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = "unavailable";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
