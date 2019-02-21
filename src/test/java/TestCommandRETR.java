import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandRETR;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandRETR {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    private String email = "bob@mail.com";
    private String input = "RETR 0";
    private String[] cmdArgs = {"RETR", "0"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandRETR(mockedDatabase);

    public TestCommandRETR(){
        when(mockedDatabase.messageExists(email, 1)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 2)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 3)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 1)).thenReturn(true);
        when(mockedDatabase.messageMarked(email, 2)).thenReturn(false);
        when(mockedDatabase.sizeOfMessage(email, 2)).thenReturn(1);
        when(mockedDatabase.getMessage(email, 2)).thenReturn("1");
    }
    @Test
    public void testCommandRETRInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRETRIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"RETR", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRETRInvalidArgType() {
        cmdArgs = new String[]{"RETR", "o"};
        assertEquals(INVALID_ARG_TYPE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRETRMessageNotFound(){
        cmdArgs = new String[]{"RETR", "3"};
        assertEquals(MESSAGE_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRETRMessageAlreadyDeleted(){
        cmdArgs = new String[]{"RETR", "1"};
        assertEquals(MESSAGE_ALREADY_DELETED + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRETRMessageMarked(){
        cmdArgs = new String[]{"RETR", "2"};
        assertEquals("+OK " + 1 + " octets\r\n1\r\n.", command.execute(input, cmdArgs, state, email));
    }
}

