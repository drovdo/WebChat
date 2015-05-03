package model;

public class Request {
    private Message message;
    private String actionToDo;

    public Request()
    {
        message = new Message();
        actionToDo = "";
    }

    public Request(Message message, String actionToDo)
    {
        this.message = message;
        this.actionToDo = actionToDo;
    }

    public Message getMessage() {
        return message;
    }

    public String getActionToDo() {
        return actionToDo;
    }

    public void setActionToDo(String actionToDo) {
        this.actionToDo = actionToDo;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
