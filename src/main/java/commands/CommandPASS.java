package commands;

import controller.CommandInterpreter.State;
import model.Database;

public class CommandPASS extends Command {

    public CommandPASS(Database database){
        super(database);
    }

    /**
     * Handles the PASS part of POP3 account authentication. Passwords can
     * contain spaces so any text after the 'PASS' text is interpreted as the
     * user's password.
     *
     * Error statuses are returned if the command is called in an invalid state,
     * the entered password is incorrect, too few arguments are passed or the
     * 'USER' command has not been executed successfully.
     *
     * @param cmd
     *            the command, split into the identifier and arguments
     * @param state
     *            the current state of user
     * @param username
     *            user identifier
     * @return the server response for the command
     */
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
