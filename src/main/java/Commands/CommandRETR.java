package Commands;

import Controller.CommandInterpreter.State;
import Model.Database;

public class CommandRETR extends Command {

    public CommandRETR(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        int id;

        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 2) {
            return INCORRECT_NUM_ARGS + input;
        }

        try {
            id = Integer.parseInt(cmd[1]);
        } catch (NumberFormatException ex) {
            return INVALID_ARG_TYPE + input;
        }

        if (!database.messageExists(username, id)) {
            return MESSAGE_NOT_FOUND + input;
        } else if (database.messageMarked(username, id)) {
            return MESSAGE_ALREADY_DELETED + input;
        } else {
            return "+OK " + database.sizeOfMessage(username, id) + " octets\r\n"
                    + database.getMessage(username, id) + "\r\n.";
        }
    }


}
