package anbd.he191271.dto;

public class VariantDTO {
    private int id;
    private String name;
    private int price;
    private String duration;

    // Constructor
    public VariantDTO(int id, String name, int price, String duration) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
