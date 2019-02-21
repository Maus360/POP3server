import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandLIST;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandLIST {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String TOO_MANY_ARGS = "-ERR too many command arguments";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    private static final String MESSAGE_MARKED = "+OK message marked as deleted";
    private String email = "bob@mail.com";
    private String input = "LIST 0";
    private String[] cmdArgs = {"LIST", "0"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandLIST(mockedDatabase);

    public TestCommandLIST(){
        when(mockedDatabase.messageExists(email, 1)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 2)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 3)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 1)).thenReturn(true);
        when(mockedDatabase.messageMarked(email, 2)).thenReturn(false);
        when(mockedDatabase.sizeOfMessage(email, 2)).thenReturn(1);
        when(mockedDatabase.getMessage(email, 2)).thenReturn("1");
        when(mockedDatabase.getNumberOfMessages(email, false)).thenReturn(2);
        when(mockedDatabase.getMaildropSize(email)).thenReturn(1);
    }
    @Test
    public void testCommandLISTInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandLISTIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"LIST", "1", "d"};
        assertEquals(TOO_MANY_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandLISTInvalidArgType() {
        cmdArgs = new String[]{"LIST", "o"};
        assertEquals(INVALID_ARG_TYPE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandLISTMessageNotFound(){
        cmdArgs = new String[]{"LIST", "1"};
        assertEquals(MESSAGE_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandLISTOkOneArgument(){
        cmdArgs = new String[]{"LIST"};
        assertEquals("+OK 2 (1)\r\n2 1\r\n.", command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandLISTOkTwoArguments(){
        cmdArgs = new String[]{"LIST", "2"};
        assertEquals("+OK 2 1", command.execute(input, cmdArgs, state, email));
    }
}