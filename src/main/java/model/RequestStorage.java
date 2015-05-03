package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RequestStorage {
    private static List<Request> history = Collections.synchronizedList(new ArrayList<Request>());

    public static List<Request> getStorage() {
        return history;
    }

    public static void addRequest(Request r) {
        history.add(r);
    }

    public static void addAll (List<Request> list) {
        history.addAll(list);
    }

    public static int getSize() {
        return history.size();
    }

    public static List<Request> getSubHistory(int index) {
        return history.subList(index, history.size());
    }

}
