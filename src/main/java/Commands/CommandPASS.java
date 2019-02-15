package Commands;

import Controller.CommandInterpreter.State;
import Model.Database;

public class CommandPASS extends Command {

    public CommandPASS(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (state != State.AUTHORIZATION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 2) {
            return INCORRECT_NUM_ARGS + input;
        } else if (username.equals("")) {
            return USER_COMMAND_NOT_SENT + input;
        }

        if (database.passwordCorrect(username, cmd[1])) {
            this.state = State.TRANSACTION;
            database.setMaildropLocked(username, true);
            return PASSWORD_OK + input;
        } else {
            return PASSWORD_INCORRECT + input;
        }
    }
}
