import Controller.POP3Server;
import View.Window;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{
    private static final String ERROR_INVALID_NUMBER_OF_ARGUMENTS = "An invalid number of arguments were specified. Usage: java Pop3Server port [timeout].";
    private static final String ERROR_INVALID_ARGUMENT = "An invalid argument was specified.";
    private static final String ERROR_INVALID_PORT = "An invalid port was specified. Port must be between 0 and 65535 inclusive.";
    private static final String ERROR_INVALID_TIMEOUT = "An invalid timeout was specified. Timeout must be greater than zero.";
    private static final String ERROR_UNABLE_TO_ESTABLISH_SOCKET = "An error occurred while establishing a socket or thread.";
    private static final int ERROR_STATUS = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Window window = new Window();
        stage.setTitle("POP3server");
        stage.setScene(window.getScene());
        stage.show();
        new Thread(() -> {
            int port = 11000, timeout = 1000;

            try {
                POP3Server server = new POP3Server(port, timeout, window);
                server.run();
            } catch (NumberFormatException e) {
                System.err.println(ERROR_INVALID_ARGUMENT);
                System.exit(ERROR_STATUS);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(ERROR_STATUS);
            }
        }).start();
    }
}
