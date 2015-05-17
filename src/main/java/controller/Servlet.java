package controller;

import model.Message;
import model.Request;
import model.RequestStorage;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;
import storage.XMLHistory;
import util.MessageExchange;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.Logger;


@WebServlet(urlPatterns = "/chat", asyncSupported = true)
public class Servlet extends HttpServlet{
    private static Logger logger = LogManager.getLogger(Servlet.class.getName());
    private Integer id = 0;
    private static List<AsyncContext> contexts = Collections.synchronizedList(new ArrayList<AsyncContext>());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = request.getParameter("flag");
        boolean flag = false;
        try {
            flag = Boolean.parseBoolean(data);
            if (flag) {
                String messages = "";
                synchronized (messages) {
                    loadHistory();
                    messages = formResponse();
                    RequestStorage.removeAll();
                }
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.print(messages);
                out.flush();
            } else {
                AsyncContext actx = request.startAsync(request, response);
                actx.setTimeout(30000);
                contexts.add(actx);
                actx.addListener(new AsyncListener() {
                    public void onTimeout(AsyncEvent arg0) throws IOException {

                        HttpServletResponse response = (HttpServletResponse) arg0.getAsyncContext().getResponse();
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        PrintWriter out = response.getWriter();
                        out.print("{\"messages\" : []}");
                        out.flush();
                        contexts.remove(arg0.getAsyncContext());
                    }

                    public void onStartAsync(AsyncEvent arg0) throws IOException {
                    }

                    public void onError(AsyncEvent arg0) throws IOException {
                    }

                    public void onComplete(AsyncEvent arg0) throws IOException {
                    }
                });
            }
        }
        catch (Exception e) {
            logger.error(e);
        }
    }

    private void doResponse(List<AsyncContext> contexts) {
        for (AsyncContext ctx : contexts) {
            HttpServletResponse response = (HttpServletResponse) ctx.getResponse();
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String messages = formResponse();
                PrintWriter out = response.getWriter();
                out.print(messages);
                out.flush();
                ctx.complete();
            } catch (IOException e) {
                logger.error(e);
            }
        }
        RequestStorage.removeAll();
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
            List<AsyncContext> contexts = new ArrayList<AsyncContext>(this.contexts);
            this.contexts.clear();
            doResponse(contexts);
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
            List<AsyncContext> contexts = new ArrayList<AsyncContext>(this.contexts);
            this.contexts.clear();
            doResponse(contexts);
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
            List<AsyncContext> contexts = new ArrayList<AsyncContext>(this.contexts);
            this.contexts.clear();
            doResponse(contexts);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Message does not exist");
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse() {
        JSONObject jsonObject = new JSONObject();
        List<Request> history = RequestStorage.getStorage();
        jsonObject.put("messages", MessageExchange.ListToJSONArray(history));
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