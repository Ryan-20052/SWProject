package anbd.he191271.entity;

public class Variant {
    private int id;
    private String name;
    private String stock_quality;
    private String duration;
    private String prpduct_id;

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

    public String getStock_quality() {
        return stock_quality;
    }

    public void setStock_quality(String stock_quality) {
        this.stock_quality = stock_quality;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPrpduct_id() {
        return prpduct_id;
    }

    public void setPrpduct_id(String prpduct_id) {
        this.prpduct_id = prpduct_id;
    }
}
