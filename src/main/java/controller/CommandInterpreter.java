package сontroller;
import commands.*;
import model.Database;

import java.util.HashMap;

public class CommandInterpreter {
    private static final String INVALID_COMMAND = "-ERR invalid command";
    static final String QUIT_ERROR = "-ERR some messages were not deleted";

    private State state;
    private Database database;
    private String username;
    private HashMap<String, Command> methods = new HashMap<>();

    public enum State {
        AUTHORIZATION, TRANSACTION, UPDATE
    };

    public CommandInterpreter() {
        state = State.AUTHORIZATION;
        database = Database.getInstance();
        username = "";

        methods.put("USER", new CommandUSER(database));
        methods.put("PASS", new CommandPASS(database));
        methods.put("QUIT", new CommandQUIT(database));
        methods.put("STAT", new CommandSTAT(database));
        methods.put("LIST", new CommandLIST(database));
        methods.put("NOOP", new CommandNOOP(database));
        methods.put("RETR", new CommandRETR(database));
        methods.put("DELE", new CommandDELE(database));
        methods.put("RSET", new CommandRSET(database));
        methods.put("TOP", new CommandTOP(database));
    }

    public String getUsername() {
        return username;
    }

    public String executeCommand(Command command, String in, String[] cmdArgs){
        return command.execute(in, cmdArgs, state, username);
    }

    public String handleInput(String input) {
        input = input.replaceAll("[\r\n]+$", "");
        String[] cmdArgs = input.split(" ", 2);
        String in = " " + input;
        cmdArgs[0].toUpperCase();
        Command result = methods.get(cmdArgs[0].toUpperCase());
        if (result == null) {
            return INVALID_COMMAND + in;
        }
        String res = executeCommand(result, in,cmdArgs);
        update(result);
        return res;
    }

    void update(Command command){
        if (command.getState() != null) state = command.getState();
        if (command.getUsername() != null) username = command.getUsername();
    }

    public void close() {
        database.restoreMarked(username);
        database.setMaildropLocked(username, false);
    }
}
