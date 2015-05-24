package dao;

import model.Message;
import model.Request;

import java.util.List;

public interface MessageDao {
    void add(Message message);

    void update(Message message);

    void delete(int id);

    int getLastId();

    void setUserId();

    List<Request> selectAll();
}
