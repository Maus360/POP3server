package сontroller.Commands;

import сontroller.CommandInterpreter.State;
import model.Database;

public class CommandUSER extends Command {

    public CommandUSER(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (state != State.AUTHORIZATION) {
            return INVALID_IN_STATE + input;
        } else if ((cmd.length != 2) || (cmd[1].split(" ").length != 1)) {
            return INCORRECT_NUM_ARGS + input;
        }

        if (database.userExists(cmd[1])) {
            if (!database.getMaildropLocked(cmd[1])) {
                this.username = cmd[1];
                return USER_OK + input;
            } else {
                return USER_LOCKED + input;
            }
        } else {
            return USER_NOT_FOUND + input;
        }
    }
}
