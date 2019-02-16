package сontroller.Commands;

import сontroller.CommandInterpreter.State;
import model.Database;

public class CommandNOOP extends Command {

    public CommandNOOP(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        } else {
            return NOOP_OK + input;
        }
    }

}
