import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Database {

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement query;
    private static Database instance = null;

    /*SQL queries*/
    private static final String QUERY_USER_EXISTS = "SELECT `email` FROM `user_credentials` WHERE `email` = ?";
    private static final String QUERY_PASSWORD_CORRECT = "SELECT `password` FROM `user_credentials` WHERE `email` = ? AND " +
            "`password` = ?";
    private static final String QUERY_NUM_OF_ALL_MAILS = "SELECT COUNT(*) FROM `mail` INNER JOIN " +
            "`user_credentials` ON `user_credentials`.`id` = `mail`.`user_credentials_id` WHERE `email` = ?";
    private static final String QUERY_MAILDROP_SIZE = "SELECT SUM(LENGTH(content)) AS `mailDropSize` FROM `mail` " +
            "INNER JOIN `user_credentials` ON `mail`.`user_credentials_id` = `user_credentials`.`id` " +
            "WHERE `email` = ? AND `markedForDeletion` = 0";
    private static final String QUERY_NUM_OF_MARKED_MAILS = "SELECT COUNT(*) FROM `mail` INNER JOIN " +
            "`user_credentials` ON `user_credentials`.`id` = `mail`.`user_credentials_id` WHERE `email` = ? AND " +
            "`markedForDeletion` = 1";
    private static final String QUERY_DELETE_MARKED_MAILS = "DELETE * FROM `mail` INNER JOIN `user_credentials` " +
            "ON `user_credentials`.`id` = `mail`.`user_credentials_id` WHERE `email` = ? AND `markedForDeletion` = 1";
    private static final String QUERY_MARK_FOR_DELETION = "UPDATE `mail` INNER JOIN `user_credentials` " +
            "ON `mail`.`user_credentials_id` = `user_credentials`.`id` SET `markedForDeletion` = 1 " +
            "WHERE `mail`.`id` = ? AND `user_credentials`.`email` = ?";
    private static final String QUERY_RESTORE_MARK_FOR_DELETION = "UPDATE `mail` INNER JOIN `user_credentials` " +
            "ON `mail`.`user_credentials_id` = `user_credentials`.`id` SET `markedForDeletion` = 0 WHERE `markedForDeletion` = 1 " +
            "AND `user_credentials`.`email` = ?";
    private static final String QUERY_SIZE_OF_SPECIFIC_MAIL = "SELECT LENGTH(content) AS `size_of_mail` FROM `mail` " +
            "WHERE `id` = ?";


    public Database() {

        try {
            connection = connectToDataBase();

            if(!connection.isClosed()){
                System.out.println("connection opened");
            }

        }
        catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error loading properties file");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection connectToDataBase() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        FileInputStream fis = null;
        fis = new FileInputStream("D:\\Учёба\\3 курс\\2 сем\\АиПОС\\POP3\\POP3server\\src\\main\\resources\\database.properties");
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
            }
        }
        return false;
    }

    public int getNumberOfAllMails(String email){
        try {
            PreparedStatement findAllMails = connection.prepareStatement(QUERY_NUM_OF_ALL_MAILS);
            findAllMails.setString(1, email);
            resultSet = findAllMails.executeQuery();
            if (resultSet.next()){
                int number = resultSet.getInt(0);
                return number;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public int getMaildropSize(String email){
        try {
            PreparedStatement maildropSize = connection.prepareStatement(QUERY_MAILDROP_SIZE);
            maildropSize.setString(1, email);
            resultSet = maildropSize.executeQuery();
            if (resultSet.next()){
                int size = resultSet.getInt("mailDropSize");
                return size;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public int getNumberOfMarkedMails(String email){
        try {
            PreparedStatement markedMails = connection.prepareStatement(QUERY_NUM_OF_MARKED_MAILS);
            markedMails.setString(1, email);
            resultSet = markedMails.executeQuery();
            if (resultSet.next()){
                int number = resultSet.getInt(0);
                return number;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
            return (numDeleted != 1) ? numDeleted : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numDeleted;
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
