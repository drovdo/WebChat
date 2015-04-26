package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MessageStorage {
    private static List<Message> history = Collections.synchronizedList(new ArrayList<Message>());

    public static List<Message> getStorage() {
        return history;
    }

    public static void addMessage(Message m) {
        history.add(m);
    }

    public static int getSize() {
        return history.size();
    }

    public static List<Message> getSubHistory(int index) {
        return history.subList(index, history.size());
    }

}
