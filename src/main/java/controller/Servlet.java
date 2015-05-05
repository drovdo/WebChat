package controller;

import model.Message;
import model.Request;
import model.RequestStorage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;
import storage.XMLHistory;
import util.MessageExchange;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


@WebServlet("/chat")
public class Servlet extends HttpServlet{
    private SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private Integer id = 0;
    @Override
    public void init() throws ServletException {
        try {
            loadHistory();
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            System.out.println(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token != null && !"".equals(token)) {
            int index = MessageExchange.getIndex(token);
            String messages = formResponse(index);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(messages);
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
            Request r = new Request();
            r.setActionToDo("POST");
            m.setDate(time);
            m.setId(id);
            r.setMessage(m);
            XMLHistory.addData(m);
            RequestStorage.addRequest(r);
            synchronized (id) {
                id++;
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException e) {
            System.out.println(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String data = MessageExchange.getMessageBody(request);
        try {
            JSONObject json = MessageExchange.getJSONObject(data);
            Message m = MessageExchange.getClientMessage(json);
            XMLHistory.deleteData(m);
            Request r = new Request();
            r.setActionToDo("DELETE");
            r.setMessage(m);
            RequestStorage.addRequest(r);
            Date time = new Date();
            System.out.println(timeFormat.format(time) + " Message with id  " + m.getId() + " is deleted");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message does not exist");
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            System.out.println(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String data = MessageExchange.getMessageBody(request);
        try {
            JSONObject json = MessageExchange.getJSONObject(data);
            Message m = MessageExchange.getClientMessage(json);
            XMLHistory.editData(m);
            Request r = new Request();
            r.setActionToDo("PUT");
            r.setMessage(m);
            RequestStorage.addRequest(r);
            Date time = new Date();
            System.out.println(timeFormat.format(time) + " Message with id  " + m.getId() + " is edited : " + m.getText());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message does not exist");
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            System.out.println(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse(int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messages", MessageExchange.ListToJSONArray(RequestStorage.getSubHistory(index)));
        jsonObject.put("token", MessageExchange.getToken(RequestStorage.getSize()));
        return jsonObject.toJSONString();
    }

    private void loadHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException  {
        if (XMLHistory.doesStorageExist()) {
            RequestStorage.addAll(XMLHistory.getMessages());
            synchronized (id) {
                id = XMLHistory.getLastId();
                id++;
            }
        } else {
            XMLHistory.createStorage();
        }
    }
}