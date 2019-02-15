package Commands;

import Controller.CommandInterpreter.State;
import Model.Database;

public abstract class Command {
    static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    static final String TOO_MANY_ARGS = "-ERR too many command arguments";
    static final String TOO_FEW_ARGS = "-ERR too few command arguments";
    static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    static final String USER_OK = "+OK found user account";
    static final String USER_LOCKED = "-ERR the maildrop is currently locked";
    static final String USER_NOT_FOUND = "-ERR user not found";
    static final String USER_COMMAND_NOT_SENT = "-ERR USER command not sent";
    static final String PASSWORD_OK = "+OK user authorised";
    static final String PASSWORD_INCORRECT = "-ERR password incorrect";
    static final String QUIT_OK = "+OK quitting";
    static final String NOOP_OK = "+OK no operation";
    static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    static final String MESSAGE_MARKED = "+OK message marked as deleted";
    static final String RESET_OK = "+OK deleted messages restored";
    static final String INVALID_ARG_VAL = "-ERR invalid argument value";
    static final String QUIT_ERROR = "-ERR some messages were not deleted";

    protected State state = null;
    protected Database database;
    protected String username = null;

    public Command(Database database){
        this.database = database;
    }
    String performUpdate(String username, State state) {
        if (state != State.UPDATE) {
            return "-ERR cannot delete outside of UPDATE state";
        }

        /* Delete marked messages */
        int numMessagesBeforeDelete = database.getNumberOfMessages(username, true);
        int n = database.deleteMarkedMails(username);

        /* Check how many messages were deleted */
        if (n == (numMessagesBeforeDelete - database.getNumberOfMessages(username, true))) {
            database.setMaildropLocked(username, false);
            return "+OK " + n + " messages deleted";
        } else {
            return QUIT_ERROR;
        }
    }

    public Database getDatabase() {
        return database;
    }

    public State getState() {
        return state;
    }

    public String getUsername() {
        return username;
    }

    public abstract String execute(String input, String[] cmd, State state, String username);
}
