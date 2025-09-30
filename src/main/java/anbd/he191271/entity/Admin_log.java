package anbd.he191271.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
@Entity
@Table(name = "admin_log")
public class Admin_log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String action;
    @Column(name = "table_affected")
    private String table_affected;
    private LocalDateTime time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTable_affected() {
        return table_affected;
    }

    public void setTable_affected(String table_affected) {
        this.table_affected = table_affected;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Admin_log(String action, String table_affected) {
        this.action = action;
        this.table_affected = table_affected;
        this.time = LocalDateTime.now();
    }
    public Admin_log() {
    }
}
