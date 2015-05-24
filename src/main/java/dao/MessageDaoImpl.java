package dao;

import controller.Servlet;
import db.ConnectionManager;
import model.Message;
import model.Request;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageDaoImpl implements MessageDao {
    private static Logger logger = LogManager.getLogger(Servlet.class.getName());
    private Integer userId = 0;

    @Override
    public void add(Message message) {
        Connection connection = null;
        PreparedStatement getUserId = null;
        PreparedStatement insertUser = null;
        PreparedStatement insertMessage = null;
        ResultSet res = null;
        int id;
        try {
            connection = ConnectionManager.getConnection();
            getUserId = connection.prepareStatement("select * from users where name = ?");
            getUserId.setString(1, message.getUser());
            res =  getUserId.executeQuery();
            if (res.next())
                id = res.getInt("id");
            else {
                insertUser = connection.prepareStatement("insert into users (id, name) values (?, ?)");
                insertUser.setInt(1, userId);
                insertUser.setString(2, message.getUser());
                insertUser.executeUpdate();
                id = userId;
                synchronized (userId) {
                    userId++;
                }
            }
            insertMessage = connection.prepareStatement("insert into messages (text, id, user_id, date) values (?, ?, ?, ?)");
            insertMessage.setString(1, message.getText());
            insertMessage.setInt(2, message.getId());
            insertMessage.setInt(3, id);
            insertMessage.setDate(4, new java.sql.Date(message.getDate().getTime()));
            insertMessage.executeUpdate();
            logger.info(message.getUser() + " (id = " + id + ") : " + message.getText() + " (id = " + message.getId() + ")");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (getUserId != null) {
                try {
                    getUserId.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (insertUser != null) {
                try {
                    insertUser.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (insertMessage != null) {
                try {
                    insertMessage.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(Message message) {
        Connection connection = null;
        PreparedStatement updateMessage = null;
        try {
            connection = ConnectionManager.getConnection();
            updateMessage = connection.prepareStatement("update messages set text = ? where id = ?");
            updateMessage.setString(1, message.getText());
            updateMessage.setInt(2, message.getId());
            updateMessage.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (updateMessage != null) {
                try {
                    updateMessage.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void delete(int id) {
        Connection connection = null;
        PreparedStatement deleteMessage = null;
        try {
            connection = ConnectionManager.getConnection();
            deleteMessage = connection.prepareStatement("delete from messages where id = ?");
            deleteMessage.setInt(1, id);
            deleteMessage.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (deleteMessage != null) {
                try {
                    deleteMessage.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public int getLastId() {
        Connection connection = null;
        Statement statement = null;
        ResultSet res = null;
        int id = -1;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            res = statement.executeQuery("select * from messages order by id desc limit 1");
            if (res.next())
                id = res.getInt("id");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return id;
    }

    @Override
    public void setUserId() {
        Connection connection = null;
        Statement statement = null;
        ResultSet res = null;
        int id = -1;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            res = statement.executeQuery("select * from users order by id desc limit 1");
            if (res.next())
                id = res.getInt("id");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        synchronized (userId) {
            userId = id;
            userId++;
        }
    }

    @Override
    public List<Request> selectAll() {
        List<Request> requests = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select * from messages inner join users on messages.user_id = users.id");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                Date date = resultSet.getDate("date");
                String user = resultSet.getString("name");
                String text = resultSet.getString("text");
                requests.add(new Request(new Message(id, user, text, date), "POST"));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return requests;
    }
}
