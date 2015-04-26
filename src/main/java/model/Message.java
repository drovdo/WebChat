package model;

public class Message {

    private int id;
    private String user;
    private String text;
    private String actionToDo;
    private boolean deleted;

    public String getAction() {
        return actionToDo;
    }

    public void setAction(String action) {
        actionToDo = action;
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
