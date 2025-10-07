package anbd.he191271.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "manager_log")
public class ManagerLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "action")
    private String action;

    @Column(name = "time", updatable = false)
    private LocalDateTime time;

    public ManagerLog(String managerName, String action) {
        this.managerName = managerName;
        this.action = action;
        this.time = LocalDateTime.now();
    }

    public ManagerLog() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
