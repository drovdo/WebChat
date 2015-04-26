package model;

import java.util.Date;

public class Message {

    private int id;
    private String user;
    private String text;
    private String actionToDo;
    private boolean deleted;
    private Date date;

    public Message() {
        id = -1;
        deleted = false;
        user = text = actionToDo = "";
    }

    public Message (int id, String user, String text, String actionToDo, boolean deleted, Date date) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.actionToDo = actionToDo;
        this.deleted = deleted;
        this.date = date;
    }

    public String getActionToDo() {
        return actionToDo;
    }

    public void setActionToDo(String actionToDo) {
        this.actionToDo = actionToDo;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setText(String text) {
        this.text = text;
    }

}
