public class CommandInterpreter {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String TOO_MANY_ARGS = "-ERR too many command arguments";
    private static final String TOO_FEW_ARGS = "-ERR too few command arguments";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String INVALID_COMMAND = "-ERR invalid command";
    private static final String USER_OK = "+OK found user account";
    private static final String USER_LOCKED = "-ERR the maildrop is currently locked";
    private static final String USER_NOT_FOUND = "-ERR user not found";
    private static final String USER_COMMAND_NOT_SENT = "-ERR USER command not sent";
    private static final String PASSWORD_OK = "+OK user authorised";
    private static final String PASSWORD_INCORRECT = "-ERR password incorrect";
    private static final String QUIT_OK = "+OK quitting";
    private static final String NOOP_OK = "+OK no operation";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    private static final String MESSAGE_MARKED = "+OK message marked as deleted";
    private static final String RESET_OK = "+OK deleted messages restored";
    private static final String INVALID_ARG_VAL = "-ERR invalid argument value";
    private static final String QUIT_ERROR = "-ERR some messages were not deleted";

    private State state;
    private Database database;
    private String username;

    private enum State {
        AUTHORIZATION, TRANSACTION, UPDATE
    };

    public CommandInterpreter() {
        state = State.AUTHORIZATION;
        database = Database.getInstance();
        username = "";
    }

    public String handleInput(String input) {
        input = input.replaceAll("[\r\n]+$", "");
        String[] cmdArgs = input.split(" ", 2);
        String in = " " + input;
        cmdArgs[0].toUpperCase();
        switch (cmdArgs[0].toUpperCase()) {
            case "USER":
                return commandUSER(in, cmdArgs);
            case "PASS":
                return commandPASS(in, cmdArgs);
            case "QUIT":
                return commandQUIT(in, cmdArgs);
            case "STAT":
                return commandSTAT(in, cmdArgs);
            case "LIST":
                return commandLIST(in, cmdArgs);
            case "RETR":
                return commandRETR(in, cmdArgs);
            case "DELE":
                return commandDELE(in, cmdArgs);
            case "NOOP":
                return commandNOOP(in, cmdArgs);
            default:
                return INVALID_COMMAND + in;
        }
    }
    private String commandUSER(String input, String[] cmd) {
        if (state != State.AUTHORIZATION) {
            return INVALID_IN_STATE + input;
        } else if ((cmd.length != 2) || (cmd[1].split(" ").length != 1)) {
            return INCORRECT_NUM_ARGS + input;
        }

        if (database.userExists(cmd[1])) {
            if (!database.getMaildropLocked(cmd[1])) {
                username = cmd[1];
                return USER_OK + input;
            } else {
                return USER_LOCKED + input;
            }
        } else {
            return USER_NOT_FOUND + input;
        }
    }

    private String commandPASS(String input, String[] cmd) {
        if (state != State.AUTHORIZATION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 2) {
            return INCORRECT_NUM_ARGS + input;
        } else if (username.equals("")) {
            return USER_COMMAND_NOT_SENT + input;
        }

        if (database.passwordCorrect(username, cmd[1])) {
            state = State.TRANSACTION;
            database.setMaildropLocked(username, true);
            return PASSWORD_OK + input;
        } else {
            return PASSWORD_INCORRECT + input;
        }
    }

    private String commandQUIT(String input, String[] cmd) {
        if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        }

        if (state == State.AUTHORIZATION) {
            return QUIT_OK + input;
        } else {
            state = State.UPDATE;
            return performUpdate() + input;
        }
    }

    private String commandNOOP(String input, String[] cmd) {
        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        } else {
            return NOOP_OK + input;
        }
    }

    private String commandSTAT(String input, String[] cmd) {
        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length != 1) {
            return INCORRECT_NUM_ARGS + input;
        }

        return "+OK " + database.numMessages(username, false) + " "
                + database.getMaildropSize(username);
    }

    private String commandLIST(String input, String[] cmd) {
        int id;

        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length > 2) {
            return TOO_MANY_ARGS + input;
        }

        if (cmd.length == 1) {
            int numMessages = database.getNumberOfMessages(username, true);
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


    private String performUpdate() {
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


    private String commandRETR(String input, String[] cmd) {
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
    private String commandDELE(String input, String[] cmd) {
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
            database.setMark(username, id, true);
            return MESSAGE_MARKED + input;
        }
    }
    public void close() {
        database.restoreMarked(username);
        database.setMaildropLocked(username, false);
    }
}
