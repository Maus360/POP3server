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
    private static final String QUERY_PASSWORD_CORRECT = "SELECT `password` FROM `user_credentials` WHERE `email` = ?, " +
            "`password` = ?";
    private static final String QUERY_NUM_OF_ALL_MAILS = "SELECT COUNT(*) FROM `mail` NATURAL JOIN " +
            "`user_credentials_id` WHERE `id` = ?";
    private static final String QUERY_NUM_OF_MARKED_MAILS = "SELECT COUNT(*) FROM `mail` NATURAL JOIN " +
            "`user_credentials_id` WHERE `id` = ?";
    private static final String QUERY_MAILDROP_SIZE = "SELECT SUM(LENGTH(content)) AS `mailDropSize` FROM `mail` " +
            "INNER JOIN `user_credentials` ON `mail`.`user_credentials_id` = `user_credentials`.`id` " +
            "WHERE `email` = ? AND `markedForDeletion` = 0";
    private static final String QUERY_DELETE_MARKED_MAILS = "DELETE `m_mail` FROM `mail` AS `m_mail` " +
            "WHERE `markedForDeletion` = 1";


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
        fis = new FileInputStream("/home/maus/bsuir/3/AiPOSiZI/POP3server/src/main/resources/database.properties");
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
}
