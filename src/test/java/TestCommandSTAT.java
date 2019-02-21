import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandSTAT;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandSTAT {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private String email = "bob@mail.com";
    private String input = "STAT";
    private String[] cmdArgs = {"STAT"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandSTAT(mockedDatabase);

    public TestCommandSTAT(){
        when(mockedDatabase.getNumberOfMessages(email, false)).thenReturn(1);
        when(mockedDatabase.getMaildropSize(email)).thenReturn(1);
    }
    @Test
    public void testCommandSTATInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandSTATIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"STAT", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandSTATOk() {
        cmdArgs = new String[]{"STAT"};
        assertEquals("+OK 1 1", command.execute(input, cmdArgs, state, email));
    }
}