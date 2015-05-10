package controller;

import model.Message;
import model.Request;
import model.RequestStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
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
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.Logger;


@WebServlet("/chat")
public class Servlet extends HttpServlet{
    private static Logger logger = LogManager.getLogger(Servlet.class.getName());
    private Integer id = 0;
    @Override
    public void init() throws ServletException {
        try {
            loadHistory();
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            logger.error(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token != null && !"".equals(token)) {
            int index = MessageExchange.getIndex(token);
            String messages = formResponse(index);
            if (messages != "Not modified") {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.print(messages);
                out.flush();
            }
            else
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error( "Bad request: 'token' parameter needed");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("doPost");
        String data = MessageExchange.getMessageBody(request);
        try {
            JSONObject json = MessageExchange.getJSONObject(data);
            Message m = MessageExchange.getClientMessage(json);
            Date time = new Date();
            logger.info(m.getUser() + " : " + m.getText());
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
            logger.info(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doDelete");
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
            logger.info("Message with id  " + m.getId() + " is deleted");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Message does not exist");
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPut");
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
            logger.info("Message with id  " + m.getId() + " is edited : " + m.getText());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Message does not exist");
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse(int index) {
        JSONObject jsonObject = new JSONObject();
        List<Request> subHistory = RequestStorage.getSubHistory(index);
        if (subHistory.size() != 0) {
            jsonObject.put("messages", MessageExchange.ListToJSONArray(subHistory));
            jsonObject.put("token", MessageExchange.getToken(RequestStorage.getSize()));
            return jsonObject.toJSONString();
        }
        return "Not modified";
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