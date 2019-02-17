package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Database {

    private final String PROPERTIES_PATH = "src/main/resources/database.properties";
    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement query;
    private static Database instance = null;

    /*SQL queries*/
    private static final String QUERY_USER_EXISTS = "SELECT `email` FROM `user_credentials` WHERE `email` = ?";
    private static final String QUERY_PASSWORD_CORRECT = "SELECT `password` FROM `user_credentials` WHERE `email` = ? AND " +
            "`password` = ?";
    private static final String QUERY_MAILDROP_SIZE = "SELECT SUM(LENGTH(content)) AS `mailDropSize` FROM `mail`, " +
            "`user_credentials` WHERE `mail`.`user_credentials_id` = `user_credentials`.`id` AND `email` = ? " +
            "AND `markedForDeletion` = 0";
    private static final String QUERY_NUM_OF_MARKED_MAILS = "SELECT COUNT(*) AS `numMsg` FROM `mail`, `user_credentials` " +
            "WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` AND `email` = ? AND `markedForDeletion` = 1";
    private static final String QUERY_NUM_OF_UMMARKED_MAILS = "SELECT COUNT(*) AS `numMsg` FROM `mail`, `user_credentials` " +
            "WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` AND `email` = ? AND `markedForDeletion` = 0";
    private static final String QUERY_DELETE_MARKED_MAILS = "DELETE `mail`.* FROM `mail`, `user_credentials` WHERE " +
            "`user_credentials`.`id` = `mail`.`user_credentials_id` AND `user_credentials`.`email` = ? " +
            "AND `mail`.`markedForDeletion` = 1";
    private static final String QUERY_UPDATE_LOCK = "UPDATE `user_credentials` SET `locked` = ? WHERE `email` = ?";
    private static final String QUERY_MAILDROP_LOCKED = "SELECT `markedForDeletion` FROM `mail`, `user_credentials` " +
            "WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` AND `email` = ? AND `markedForDeletion` = 1";
    private static final String QUERY_RESTORE_MARK_FOR_DELETION = "UPDATE `mail`, `user_credentials` SET " +
            "`markedForDeletion` = 1 WHERE `markedForDeletion` = 0 AND `mail`.`user_credentials_id` = `user_credentials`.`id` " +
            "AND `user_credentials`.`email` = ?;";
    private static final String QUERY_MESSAGE_EXISTS = "SELECT * from (select ROW_NUMBER() OVER(ORDER BY `mail`.`id` ASC) " +
            "as rowN FROM `mail`, `user_credentials` WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` AND " +
            "`user_credentials`.`email` = ?) as idTable where `idTable`.`rowN` = ?";
    private static final String QUERY_MESSAGE_CONTENT = "select `content` from (select ROW_NUMBER() " +
            "OVER(ORDER BY `mail`.`id` ASC) as rowN, `mail`.`content` FROM `mail`, `user_credentials` where " +
            "`user_credentials`.`id` = `mail`.`user_credentials_id` AND `user_credentials`.`email` = ?) as idTable " +
            "where `idTable`.`rowN` = ?";
    private static final String QUERY_MESSAGE_SIZE = "select LENGTH(content) AS `size_of_mail` from " +
            "(select ROW_NUMBER() OVER(ORDER BY `mail`.`id` ASC) as rowN, `content` FROM `mail`, `user_credentials` WHERE " +
            "`user_credentials`.`id` = `mail`.`user_credentials_id` AND `user_credentials`.`email` = ?) as idTable " +
            "where `idTable`.`rowN` = ?";
    private static final String QUERY_MESSAGE_MARKED = "select `markedForDeletion` from " +
            "(select ROW_NUMBER() OVER(ORDER BY `mail`.`id` ASC) as rowN, `markedForDeletion` FROM `mail`, `user_credentials` " +
            "WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` AND `user_credentials`.`email` = ? AND " +
            "`mail`.`markedForDeletion` = 1) as idTable where `idTable`.`rowN` = ?;";
    private static final String QUERY_UPDATE_MARK = "UPDATE `mail` AS `m` JOIN (SELECT ROW_NUMBER() OVER(ORDER BY " +
            "`mail`.`id` ASC) as rownum, `mail`.`id`, `mail`.`content` FROM `mail` INNER JOIN `user_credentials` ON " +
            "`mail`.`user_credentials_id` = `user_credentials`.`id` WHERE `user_credentials`.`email` = ?) AS r " +
            "ON m.id = r.id SET `markedForDeletion` = ? where r.rownum = ?;";
    private static final String QUERY_MESSAGE_UIDL = "select `UIDL` from (select ROW_NUMBER() OVER(ORDER BY `mail`.`id` ASC) " +
            "as rowN, `UIDL` FROM `mail`, `user_credentials` WHERE `user_credentials`.`id` = `mail`.`user_credentials_id` " +
            "AND `user_credentials`.`email` = ?) as idTable where `idTable`.`rowN` = ?";

    public Database() {
        try {
            connection = connectToDataBase();
            if(!connection.isClosed()){
                System.out.println("Connection Opened");
            }
        }
        catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error loading properties file");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection connectToDataBase() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(PROPERTIES_PATH);
        properties.load(fis);
        Class.forName(properties.getProperty("db_driver_class"));
        connection = DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
        return connection;
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public boolean userExists(String email){
        try {
            query = connection.prepareStatement(QUERY_USER_EXISTS);
            query.setString(1, email);
            return query.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("User does not exist");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean passwordCorrect(String email, String password){
        if (userExists(email)){
            try {
                query = connection.prepareStatement(QUERY_PASSWORD_CORRECT);
                query.setString(1, email);
                query.setString(2, password);
                return query.executeQuery().next();
            } catch (SQLException e) {
                System.out.println("Password is not correct");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public int getMaildropSize(String email){
        try {
            PreparedStatement maildropSize = connection.prepareStatement(QUERY_MAILDROP_SIZE);
            maildropSize.setString(1, email);
            resultSet = maildropSize.executeQuery();
            if (!resultSet.next()){
                return 0;
            }
            return resultSet.getInt("mailDropSize");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getNumberOfMessages(String email, boolean deleted){
        try {
            query = connection.prepareStatement(deleted ? QUERY_NUM_OF_MARKED_MAILS : QUERY_NUM_OF_UMMARKED_MAILS);
            query.setString(1, email);
            resultSet = query.executeQuery();
            if (!resultSet.next()){
                return 0;
            }
            return Integer.parseInt(resultSet.getString("numMsg"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int deleteMarkedMails(String email){
        int numDeleted = 0;
        try {
            query = connection.prepareStatement(QUERY_DELETE_MARKED_MAILS);
            query.setString(1, email);
            query.executeUpdate();

            numDeleted = query.getUpdateCount();
            return (numDeleted != -1) ? numDeleted : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numDeleted;
    }

    public void setMaildropLocked(String username, boolean b) {
        try {
            query = connection.prepareStatement(QUERY_UPDATE_LOCK);
            query.setInt(1, b ? 1 : 0);
            query.setString(2, username);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean messageExists(String email, int id){
        try{
            query = connection.prepareStatement(QUERY_MESSAGE_EXISTS);
            query.setString(1, email);
            query.setInt(2, id);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getMaildropLocked(String email) {
        try {
            query = connection.prepareStatement(QUERY_MAILDROP_LOCKED);
            query.setString(1, email);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void restoreMarked(String username) {
        try {
            query = connection.prepareStatement(QUERY_RESTORE_MARK_FOR_DELETION);
            query.setString(1, username);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String username, int id) {
        try {
            query = connection.prepareStatement(QUERY_MESSAGE_CONTENT);
            query.setString(1, username);
            query.setInt(2, id);
            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getString("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int sizeOfMessage(String email, int id) {
        try {
            query = connection.prepareStatement(QUERY_MESSAGE_SIZE);
            query.setString(1, email);
            query.setInt(2, id);

            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt("size_of_mail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean messageMarked(String email, int id) {
        try {
            query = connection.prepareStatement(QUERY_MESSAGE_MARKED);
            query.setString(1, email);
            query.setInt(2, id);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setMark(String email, int id, boolean marked) {
        try {
            query = connection.prepareStatement(QUERY_UPDATE_MARK);
            query.setString(1, email);
            query.setInt(2, marked ? 1 : 0);
            query.setInt(3, id);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUIDL(String email, int id){
        try {
            query = connection.prepareStatement(QUERY_MESSAGE_UIDL);
            query.setString(1, email);
            query.setInt(2, id);

            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close(){
        query = null;
        resultSet = null;
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
