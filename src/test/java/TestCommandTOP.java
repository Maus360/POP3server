import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandTOP;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandTOP {
    private static final String TOO_FEW_ARGS = "-ERR too few command arguments";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    static final String INVALID_ARG_VAL = "-ERR invalid argument value";
    private String email = "bob@mail.com";
    private String input = "TOP 1 1";
    private String[] cmdArgs = {"TOP", "1 1"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandTOP(mockedDatabase);

    public TestCommandTOP(){
        when(mockedDatabase.getNumberOfMessages(email, false)).thenReturn(1);
        when(mockedDatabase.getMaildropSize(email)).thenReturn(1);
        when(mockedDatabase.messageExists(email, 1)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 2)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 3)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 1)).thenReturn(true);
        when(mockedDatabase.messageMarked(email, 2)).thenReturn(false);
        when(mockedDatabase.sizeOfMessage(email, 2)).thenReturn(1);
        when(mockedDatabase.getMessage(email, 2)).thenReturn("1\n\n1");
    }
    @Test
    public void testCommandTOPInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"TOP", "1 1 1"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPTooFewArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"TOP"};
        assertEquals(TOO_FEW_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPInvalidArgType() {
        cmdArgs = new String[]{"TOP","a 1"};
        assertEquals(INVALID_ARG_TYPE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPMessageNotFound(){
        cmdArgs = new String[]{"TOP","3 1"};
        assertEquals(MESSAGE_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPMessageAlreadyDeleted(){
        cmdArgs = new String[]{"TOP","1 1"};
        assertEquals(MESSAGE_ALREADY_DELETED + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPInvalidArgVal(){
        cmdArgs = new String[]{"TOP","2 -1"};
        assertEquals(INVALID_ARG_VAL + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandTOPOk(){
        cmdArgs = new String[]{"TOP","2 1"};
        assertEquals("+OK \r\n1\r\n\n1\r\n.", command.execute(input, cmdArgs, state, email));
    }
}