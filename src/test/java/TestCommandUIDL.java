import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandUIDL;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandUIDL {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String TOO_MANY_ARGS = "-ERR too many command arguments";
    private static final String INVALID_ARG_TYPE = "-ERR invalid argument type";
    private static final String MESSAGE_NOT_FOUND = "-ERR message not found";
    private static final String MESSAGE_ALREADY_DELETED = "-ERR message already deleted";
    private static final String MESSAGE_MARKED = "+OK message marked as deleted";
    private String email = "bob@mail.com";
    private String input = "UIDL 1";
    private String[] cmdArgs = {"UIDL", "1"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandUIDL(mockedDatabase);

    public TestCommandUIDL(){
        when(mockedDatabase.messageExists(email, 1)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 2)).thenReturn(true);
        when(mockedDatabase.messageExists(email, 3)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 1)).thenReturn(false);
        when(mockedDatabase.messageMarked(email, 2)).thenReturn(true);
        when(mockedDatabase.sizeOfMessage(email, 1)).thenReturn(1);
        when(mockedDatabase.getMessage(email, 1)).thenReturn("1");
        when(mockedDatabase.getNumberOfMessages(email, false)).thenReturn(1);
        when(mockedDatabase.getMaildropSize(email)).thenReturn(1);
        when(mockedDatabase.getUIDL(email, 1)).thenReturn("abrakadabra");
    }
    @Test
    public void testCommandUIDLInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUIDLIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"UIDL", "1", "d"};
        assertEquals(TOO_MANY_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUIDLInvalidArgType() {
        cmdArgs = new String[]{"UIDL", "o"};
        assertEquals(INVALID_ARG_TYPE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUIDLMessageNotFound(){
        cmdArgs = new String[]{"UIDL", "2"};
        assertEquals(MESSAGE_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUIDLOkOneArgument(){
        cmdArgs = new String[]{"UIDL"};
        assertEquals("+OK 1 (1)\r\n1 abrakadabra\r\n.", command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUIDLOkTwoArguments(){
        cmdArgs = new String[]{"UIDL", "1"};
        assertEquals("+OK 1 abrakadabra", command.execute(input, cmdArgs, state, email));
    }
}