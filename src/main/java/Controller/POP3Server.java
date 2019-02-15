package Controller;

import Model.Database;
import View.Window;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.IllegalBlockingModeException;

public class POP3Server {
    private static final String ERROR_INVALID_NUMBER_OF_ARGUMENTS = "An invalid number of arguments were specified. Usage: java Pop3Server port [timeout].";
    private static final String ERROR_INVALID_ARGUMENT = "An invalid argument was specified.";
    private static final String ERROR_INVALID_PORT = "An invalid port was specified. Port must be between 0 and 65535 inclusive.";
    private static final String ERROR_INVALID_TIMEOUT = "An invalid timeout was specified. Timeout must be greater than zero.";
    private static final String ERROR_UNABLE_TO_ESTABLISH_SOCKET = "An error occurred while establishing a socket or thread.";
    private static final int ERROR_CODE = 1;
    private Window window;
    private int port;
    private int timeout;
    private boolean serverRunning;

    public POP3Server(int port, int timeout, Window window) throws IllegalArgumentException{
        this.window = window;
        this.port = port;
        this.timeout = timeout;

        if (timeout <= 0){
            throw new IllegalArgumentException(ERROR_INVALID_TIMEOUT);
        }

        if (port < 0 || port > 65535){
            throw new IllegalArgumentException(ERROR_INVALID_PORT);
        }
    }

    public void run() {
        serverRunning = true;

        /*
         * Attempt to open a ServerSocket. Also closes the socket when the
         * try/catch is complete
         */
        try (ServerSocket socket = new ServerSocket(port)) {
            while (serverRunning) {
                /*
                 * Create and start a new Controller.ServerThread. A reference to the
                 * thread isn't needed as the Garbage Collector will clean it up
                 * after the client quits or the session times out.
                 */
                new ServerThread(socket.accept(), timeout, window).start();
            }
        } catch (IOException | SecurityException | IllegalBlockingModeException
                | IllegalArgumentException ex) {
            System.err.println(ERROR_UNABLE_TO_ESTABLISH_SOCKET);
        } finally {
            /* Close the database connection */
            Database db = Database.getInstance();
            db.close();
            db = null;
        }
    }
}
