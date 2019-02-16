package сontroller.Commands;

import сontroller.CommandInterpreter.State;
import model.Database;

public class CommandRSET extends Command {

    public CommandRSET(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        } else {
            database.restoreMarked(username);
            return RESET_OK + input;
        }
    }
}
