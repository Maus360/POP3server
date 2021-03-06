import controller.POP3Server;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import view.Window;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main extends Application{
    private static Logger log = Logger.getLogger("Main");
    private final String PROPERTIES_PATH = "server.properties";
    private static final String ERROR_INVALID_NUMBER_OF_ARGUMENTS = "An invalid number of arguments were specified. Usage: java Pop3Server port [timeout].";
    private static final String ERROR_INVALID_ARGUMENT = "An invalid argument was specified.";
    private static final String ERROR_INVALID_PORT = "An invalid port was specified. Port must be between 0 and 65535 inclusive.";
    private static final String ERROR_INVALID_TIMEOUT = "An invalid timeout was specified. Timeout must be greater than zero.";
    private static final String ERROR_UNABLE_TO_ESTABLISH_SOCKET = "An error occurred while establishing a socket or thread.";
    private static final int ERROR_STATUS = 1;
    private static final int TIMEOUT = 1000;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        File jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = jarPath.getParentFile().getAbsolutePath();
        PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
        PropertyConfigurator.configureAndWatch(path + "/log4j.properties");

        log.info("Starting POP3 Server");
        Window window = new Window();
        stage.setTitle("POP3server");
        stage.setScene(window.getScene());
        stage.show();
        new Thread(() -> {
            int port = readProperties();
            try {
                POP3Server server = new POP3Server(port, TIMEOUT, window);
                server.run();
            } catch (NumberFormatException e) {
                log.error(ERROR_INVALID_ARGUMENT);
                log.trace(e.getStackTrace());
                System.exit(ERROR_STATUS);
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
                log.trace(e.getStackTrace());
                System.exit(ERROR_STATUS);
            }
        }).start();
    }

    public int readProperties() {
        log.info("Reading server properties");
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            File jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String path = jarPath.getParentFile().getAbsolutePath();
            fis = new FileInputStream(path + "/" + PROPERTIES_PATH);
            properties.load(fis);
        } catch (IOException e) {
            log.error("Failed reading server properties");
            log.trace(e.getStackTrace());
        }
        return Integer.parseInt(properties.getProperty("port"));
    }
}
