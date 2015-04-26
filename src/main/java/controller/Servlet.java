package controller;

import model.Message;
import model.MessageStorage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import util.MessageExchange;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


@WebServlet("/chat")
public class Servlet extends HttpServlet{
    private SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
    @Override
    public void init() throws ServletException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token != null && !"".equals(token)) {
            int index = MessageExchange.getIndex(token);
            String tasks = formResponse(index);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(tasks);
            out.flush();
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = MessageExchange.getMessageBody(request);
        try {
            JSONObject json = MessageExchange.getJSONObject(data);
            Message m = MessageExchange.getClientMessage(json);
            Date time = new Date();
            System.out.println(timeFormat.format(time) + " " + m.getUser() + " : " + m.getText());
            m.setAction("POST");
            MessageStorage.addMessage(m);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse(int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messages", MessageExchange.ListToJSONArray(MessageStorage.getSubHistory(index)));
        jsonObject.put("token", MessageExchange.getToken(MessageStorage.getSize()));
        return jsonObject.toJSONString();
    }
}