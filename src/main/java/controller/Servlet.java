package controller;

import dao.MessageDao;
import dao.MessageDaoImpl;
import model.Message;
import model.Request;
import model.RequestStorage;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import util.MessageExchange;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private MessageDao messageDao = new MessageDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = request.getParameter("flag");
        try {
            if (data != null && !"".equals(data)) {
                boolean flag = false;
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
            else
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
            Request r = new Request();
            r.setActionToDo("POST");
            m.setDate(time);
            m.setId(id);
            r.setMessage(m);
            messageDao.add(m);
            RequestStorage.addRequest(r);
            synchronized (id) {
                id++;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            List<AsyncContext> contexts = new ArrayList<AsyncContext>(this.contexts);
            this.contexts.clear();
            doResponse(contexts);
        } catch (ParseException e) {
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
            messageDao.delete(m.getId());
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
        } catch (ParseException e) {
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
            messageDao.update(m);
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
        } catch (ParseException e) {
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

    private void loadHistory() throws IOException  {
        RequestStorage.addAll(messageDao.selectAll());
        messageDao.setUserId();
        synchronized (id) {
            id = messageDao.getLastId();
            id++;
        }
    }
}