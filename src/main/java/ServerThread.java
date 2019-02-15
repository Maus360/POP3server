import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerThread extends Thread {
    private static final String INFO_USER_TIMEOUT = "User timed out";
    private static final String INFO_USER_CONNECTED = "User connected";
    private static final String INFO_USER_DISCONNECTED = "User disconnected";
    private static final String SERVER_WELCOME = "+OK POP3 server ready";
    private static final String ERROR_STREAM = "Unable to open or close the network stream.";
    private static final String ERROR_SOCKET_STREAM_CLOSE = "Unable to close a socket or stream.";
    private Window window;

    private Socket socket;
    private InputStreamReader streamReader;
    private PrintWriter out;
    private BufferedReader in;
    private CommandInterpreter interpreter;

    public ServerThread(Socket socket, int timeout, Window window) throws SocketException {
        super("Pop3ServerThread <" + socket.getInetAddress() + ">");

        this.window = window;
        this.socket = socket;
        interpreter = new CommandInterpreter();
        this.socket.setSoTimeout(timeout * 1000);

        System.out.println("[" + socket.getInetAddress() + "] "
                + INFO_USER_CONNECTED);
    }

    @Override
    public void run() {
        try {

            streamReader = new InputStreamReader(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(streamReader);
            String input, output;

            out.println(SERVER_WELCOME);
            window.print("Server to [" + socket.getInetAddress() + "]: " + SERVER_WELCOME);

            while ((input = in.readLine()) != null) {
                window.print("[" + socket.getInetAddress() + "] to Server: " + input);
                output = interpreter.handleInput(input);
                out.println(output);
                window.print("Server to [" + socket.getInetAddress() + "]: " + output);

                if (input.toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            interpreter.close();
            System.out.println("[" + socket.getInetAddress() + "] "
                    + INFO_USER_TIMEOUT);
        } catch (IOException e) {
            System.err.println(ERROR_STREAM);
        } finally {
            try {
                streamReader.close();
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.err.println(ERROR_SOCKET_STREAM_CLOSE);
            } finally {
                System.out.println("[" + socket.getInetAddress() + "] "
                        + INFO_USER_DISCONNECTED);
            }
        }
    }
}