import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import controller.CommandInterpreter.State;
import commands.Command;
import commands.CommandUSER;
import model.Database;
import org.junit.Test;

public class TestCommandUSER {
    private static final String INVALID_IN_STATE = "-ERR command invalid in the current state";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private static final String USER_OK = "+OK found user account";
    private static final String USER_LOCKED = "-ERR the maildrop is currently locked";
    private static final String USER_NOT_FOUND = "-ERR user not found";
    private String email = "bob@mail.com";
    private String input = "USER " + email;
    private String[] cmdArgs = {"USER", email};
    private State state = State.AUTHORIZATION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandUSER(mockedDatabase);

    public TestCommandUSER(){
        when(mockedDatabase.userExists(email)).thenReturn(true);
        when(mockedDatabase.getMaildropLocked(email)).thenReturn(false);
        when(mockedDatabase.userExists("b0b"+email)).thenReturn(false);
        when(mockedDatabase.userExists("admin"+email)).thenReturn(true);
        when(mockedDatabase.getMaildropLocked("admin"+email)).thenReturn(true);
    }
    @Test
    public void testCommandUSERInvalidInState(){
        state = State.TRANSACTION;
        assertEquals(INVALID_IN_STATE + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUSERIncorrectNumArgs(){
        cmdArgs = new String[]{"USER", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUSERUserLocked() {
        cmdArgs = new String[]{"USER", "admin"+email};
        assertEquals(USER_LOCKED + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUSERUserNotFound(){
        cmdArgs = new String[]{"USER", "bob"+email};
        assertEquals(USER_NOT_FOUND + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandUSEROk(){
        cmdArgs = new String[]{"USER", email};
        assertEquals(USER_OK + input, command.execute(input, cmdArgs, state, email));
    }
}
