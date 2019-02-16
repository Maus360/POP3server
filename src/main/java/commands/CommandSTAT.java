package commands;

import controller.CommandInterpreter.State;
import model.Database;

public class CommandSTAT extends Command {

    public CommandSTAT(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        }

        return "+OK " + database.getNumberOfMessages(username, false) + " "
                + database.getMaildropSize(username);
    }

}
