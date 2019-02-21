import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandRSET;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandRSET {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String RESET_OK = "+OK deleted messages restored";
    private String email = "bob@mail.com";
    private String input = "RSET 0";
    private String[] cmdArgs = {"RSET", "0"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandRSET(mockedDatabase);

    public TestCommandRSET(){}
    @Test
    public void testCommandRSETInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRSETIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"RSET", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandRSETOk() {
        cmdArgs = new String[]{"RSET"};
        assertEquals(RESET_OK + input, command.execute(input, cmdArgs, state, email));
    }
}