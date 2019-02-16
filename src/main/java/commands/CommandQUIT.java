package commands;

import controller.CommandInterpreter.State;
import model.Database;

public class CommandQUIT extends Command {

    public CommandQUIT(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        }

        if (state == State.AUTHORIZATION) {
            return QUIT_OK + input;
        } else {
            this.state = State.UPDATE;
            return performUpdate(username, this.state) + input;
        }
    }


}
