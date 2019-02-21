import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandPASS;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;

public class TestCommandPASS {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String USER_COMMAND_NOT_SENT = "-ERR USER command not sent";
    private static final String PASSWORD_OK = "+OK user authorised";
    private static final String PASSWORD_INCORRECT = "-ERR password incorrect";
    private String email = "bob@mail.com";
    private String input = "PASS abc";
    private String[] cmdArgs = {"PASS", "abc"};
    private State state = State.AUTHORIZATION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandPASS(mockedDatabase);

    public TestCommandPASS(){
        when(mockedDatabase.passwordCorrect(email, "abc")).thenReturn(true);
    }
    @Test
    public void testCommandPASSInvalidInState(){
        state = State.TRANSACTION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandPASSIncorrectNumArgs(){
        cmdArgs = new String[]{"PASS", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandPASSUserCommandNotSend() {
        cmdArgs = new String[]{"PASS", "abc"};
        assertEquals(USER_COMMAND_NOT_SENT + input, command.execute(input, cmdArgs, state, ""));
    }
    @Test
    public void testCommandPASSOk(){
        cmdArgs = new String[]{"PASS", "abc"};
        assertEquals(PASSWORD_OK + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandPASSIncorrect(){
        cmdArgs = new String[]{"PASS", "abd"};
        assertEquals(PASSWORD_INCORRECT +  input, command.execute(input, cmdArgs, state, email));
    }
}