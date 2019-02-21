import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import controller.CommandInterpreter.State;
import commands.Command;
import commands.CommandDELE;
import model.Database;
import org.junit.Test;

public class TestCommandDELE {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    private static final String MESSAGE_MARKED = "+OK message marked as deleted";
    private String email = "bob@mail.com";
    private String input = "DELE 0";
    private String[] cmdArgs = {"DELE", "0"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandDELE(mockedDatabase);

    public TestCommandDELE(){
        when(mockedDatabase.messageExists(email, 1)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 2)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 3)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 1)).thenReturn(true);
        when(mockedDatabase.messageMarked(email, 2)).thenReturn(false);
    }
    @Test
    public void testCommandDELEInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandDELEIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"DELE", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandDELEInvalidArgType() {
        cmdArgs = new String[]{"DELE", "o"};
        assertEquals(INVALID_ARG_TYPE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandDELEMessageNotFound(){
        cmdArgs = new String[]{"DELE", "3"};
        assertEquals(MESSAGE_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandDELEMessageAlreadyDeleted(){
        cmdArgs = new String[]{"DELE", "1"};
        assertEquals(MESSAGE_ALREADY_DELETED + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandDELEMessageMarked(){
        cmdArgs = new String[]{"DELE", "2"};
        assertEquals(MESSAGE_MARKED + input, command.execute(input, cmdArgs, state, email));
    }
}
