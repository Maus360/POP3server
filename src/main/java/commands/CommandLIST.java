package commands;

import controller.CommandInterpreter.State;
import model.Database;

public class CommandLIST extends Command {

    public CommandLIST(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        int id;

        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length > 2) {
            return TOO_MANY_ARGS + input;
        }

        if (cmd.length == 1) {
            int numMessages = database.getNumberOfMessages(username, false);
            String out = "+OK " + database.getNumberOfMessages(username, false) + " ("
                    + database.getMaildropSize(username) + ")\r\n";
            for (int i = 1; i <= numMessages; i++) {
                if (!database.messageMarked(username, i)) {
                    out += i + " " + database.sizeOfMessage(username, i) + "\r\n";
                }
            }
            return out + ".";
        } else {
            try {
                id = Integer.parseInt(cmd[1]);
            } catch (Exception ex) {
                return INVALID_ARG_TYPE + input;
            }

            if (database.messageExists(username, id) && !database.messageMarked(username, id)) {
                return "+OK " + id + " " + database.sizeOfMessage(username, id);
            } else {
                return MESSAGE_NOT_FOUND + input;
            }
        }
    }


}
