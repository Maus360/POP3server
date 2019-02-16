package commands;

import controller.CommandInterpreter.State;
import model.Database;

public class CommandTOP extends Command {

    public CommandTOP(Database database){
        super(database);
    }

    @Override
    public String execute(String input, String[] cmd, State state, String username) {
        int id, n;
        String[] args;

        if (state != State.TRANSACTION) {
            return INVALID_IN_STATE + input;
        } else if (cmd.length < 2) {
            return TOO_FEW_ARGS + input;
        } else {
            args = cmd[1].split(" ");
            if (args.length != 2) {
                return INCORRECT_NUM_ARGS + input;
            }
        }

        try {
            id = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            return INVALID_ARG_TYPE + input;
        }

        if (!database.messageExists(username, id)) {
            return MESSAGE_NOT_FOUND + input;
        } else if (database.messageMarked(username, id)) {
            return MESSAGE_ALREADY_DELETED + input;
        } else if (n < 0) {
            return INVALID_ARG_VAL + input;
        } else {
            String message = database.getMessage(username, id);

            /* Check for an empty message */
            if (message.equals(null)) {
                return "+OK\r\n.";
            }

            /* Split the full message into header and body components */
            String[] tmp = message.split("\n\n", 2);
            String header = tmp[0];
            String body = tmp[1];

            /* Split the body into lines */
            tmp = body.split("\n");
            body = "";

            /* Reconstruct the body for n lines */
            for (int i = 0; i <= tmp.length - 1; i++) {
                if (n == i) break;
                body += "\n" + tmp[i];
            }

            /* Return the requested lines */
            return "+OK \r\n" + header + "\r\n" + body + "\r\n.";
        }
    }


}
