package model;

import org.apache.log4j.Logger;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class Database {
    private static Logger log = Logger.getLogger(Database.class.getName());
    private final String PROPERTIES_PATH = "database.properties";
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
                log.info("Connection Opened");
            }
        }
        catch (SQLException e){
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState: " + e.getSQLState());
            log.error("VendorError: " + e.getErrorCode());
            e.printStackTrace();
            log.trace(e.getStackTrace());
        } catch (FileNotFoundException e) {
            log.error("File not found");
            e.printStackTrace();
            log.trace(e.getStackTrace());
        } catch (IOException e) {
            log.error("Error loading properties file");
            e.printStackTrace();
            log.trace(e.getStackTrace());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
    }

    /**
     * Connect to database via property data of url, username, password.
     * @return Connected driver to database
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private Connection connectToDataBase() throws IOException, ClassNotFoundException, SQLException {
        log.info("Establishing connection to database...");
        Properties properties = new Properties();
        File jarPath = new File(Database.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = jarPath.getParentFile().getAbsolutePath();
        FileInputStream fis = new FileInputStream(path + "/" + PROPERTIES_PATH);
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
            log.info("Check if user exists");
            query = connection.prepareStatement(QUERY_USER_EXISTS);
            query.setString(1, email);
            return query.executeQuery().next();
        } catch (SQLException e) {
            log.error("User does not exist");
            System.out.println(e.getMessage());
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return false;
    }

    public boolean passwordCorrect(String email, String password){
        if (userExists(email)){
            try {
                log.info("Verifying password");
                query = connection.prepareStatement(QUERY_PASSWORD_CORRECT);
                query.setString(1, email);
                query.setString(2, password);
                return query.executeQuery().next();
            } catch (SQLException e) {
                log.error("Password is not correct");
                System.out.println(e.getMessage());
                e.printStackTrace();
                log.trace(e.getStackTrace());
            }
        }
        return false;
    }

    public int getMaildropSize(String email){
        try {
            log.info("Getting maildrop size");
            PreparedStatement maildropSize = connection.prepareStatement(QUERY_MAILDROP_SIZE);
            maildropSize.setString(1, email);
            resultSet = maildropSize.executeQuery();
            if (!resultSet.next()){
                log.info("No mails found in " + email + " inbox");
                return 0;
            }
            return resultSet.getInt("mailDropSize");
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return 0;
    }

    public int getNumberOfMessages(String email, boolean deleted){
        try {
            log.info("Getting number of messages");
            query = connection.prepareStatement(deleted ? QUERY_NUM_OF_MARKED_MAILS : QUERY_NUM_OF_UMMARKED_MAILS);
            query.setString(1, email);
            resultSet = query.executeQuery();
            if (!resultSet.next()){
                log.info("No " + (deleted ? "marked" : "unmarked") + "for deletion mails found in " + email + " inbox");
                return 0;
            }
            return Integer.parseInt(resultSet.getString("numMsg"));
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return 0;
    }

    public int deleteMarkedMails(String email){
        int numDeleted = 0;
        try {
            log.info("Getting the number of deleted mails");
            query = connection.prepareStatement(QUERY_DELETE_MARKED_MAILS);
            query.setString(1, email);
            query.executeUpdate();

            numDeleted = query.getUpdateCount();
            return (numDeleted != -1) ? numDeleted : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return numDeleted;
    }

    public void setMaildropLocked(String username, boolean b) {
        try {
            log.info("Locking " + username + "maildrop");
            query = connection.prepareStatement(QUERY_UPDATE_LOCK);
            query.setInt(1, b ? 1 : 0);
            query.setString(2, username);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
    }

    public boolean messageExists(String email, int id){
        try{
            log.info("Checking if message #" + id + " exists in " + email + " inbox");
            query = connection.prepareStatement(QUERY_MESSAGE_EXISTS);
            query.setString(1, email);
            query.setInt(2, id);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return false;
    }

    public boolean getMaildropLocked(String email) {
        try {
            log.info("Getting status of " + email + "maildrop");
            query = connection.prepareStatement(QUERY_MAILDROP_LOCKED);
            query.setString(1, email);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return true;
    }

    public void restoreMarked(String username) {
        try {
            log.info("Restoring marked for deletion mails for " + username);
            query = connection.prepareStatement(QUERY_RESTORE_MARK_FOR_DELETION);
            query.setString(1, username);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
    }

    public String getMessage(String username, int id) {
        try {
            log.info("Getting content of message #" + id);
            query = connection.prepareStatement(QUERY_MESSAGE_CONTENT);
            query.setString(1, username);
            query.setInt(2, id);
            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getString("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return null;
    }

    public int sizeOfMessage(String email, int id) {
        try {
            log.info("Getting size of message #" + id);
            query = connection.prepareStatement(QUERY_MESSAGE_SIZE);
            query.setString(1, email);
            query.setInt(2, id);

            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt("size_of_mail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return 0;
    }

    public boolean messageMarked(String email, int id) {
        try {
            log.info("Checking if message #" + id + "marked for deletion");
            query = connection.prepareStatement(QUERY_MESSAGE_MARKED);
            query.setString(1, email);
            query.setInt(2, id);
            return query.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
        return false;
    }

    public void setMark(String email, int id, boolean marked) {
        try {
            log.info("Setting message" + (marked ? "marked" : "unmarked") + " for deletion");
            query = connection.prepareStatement(QUERY_UPDATE_MARK);
            query.setString(1, email);
            query.setInt(2, marked ? 1 : 0);
            query.setInt(3, id);
            query.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
        }
    }

    public String getUIDL(String email, int id){
        try {
            log.info("Getting UIDL of the message #" + id);
            query = connection.prepareStatement(QUERY_MESSAGE_UIDL);
            query.setString(1, email);
            query.setInt(2, id);

            resultSet = query.executeQuery();
            if (resultSet.next()){
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace(e.getStackTrace());
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
            log.trace(e.getStackTrace());
        }
    }
}
