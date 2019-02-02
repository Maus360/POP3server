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
        fis = new FileInputStream("~/bsuir/3/AiPOSiZI/POP3server/src/main/resources/database.properties");
        properties.load(fis);
        Class.forName(properties.getProperty("db_driver_class"));
        connection = DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));

        return connection;
    }

    public int myClass(int a, int b){
        return a - b;
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
}
