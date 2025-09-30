package anbd.he191271.entity;

import java.util.Date;

public class Admin_log {
    private int id;
    private String action;
    private String table_affected;
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
