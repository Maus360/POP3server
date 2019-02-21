import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandNOOP;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandNOOP {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String NOOP_OK = "+OK no operation";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private String email = "bob@mail.com";
    private String input = "NOOP 0";
    private String[] cmdArgs = {"NOOP"};
    private State state = State.TRANSACTION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandNOOP(mockedDatabase);

    public TestCommandNOOP(){}
    @Test
    public void testCommandNOOPInvalidInState(){
        state = State.AUTHORIZATION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandNOOPIncorrectNumArgs(){
        state = State.TRANSACTION;
        cmdArgs = new String[]{"NOOP", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandNOOPInvalidArgType() {
        cmdArgs = new String[]{"NOOP"};
        assertEquals(NOOP_OK + input, command.execute(input, cmdArgs, state, email));
    }
}