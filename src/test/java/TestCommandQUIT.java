import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import commands.CommandQUIT;
import controller.CommandInterpreter.State;
import commands.Command;
import model.Database;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestCommandQUIT {
    private static final String QUIT_ERROR = "-ERR some messages were not deleted";
    private static final String QUIT_OK = "+OK quitting";
    private static final String INCORRECT_NUM_ARGS = "-ERR incorrect number of arguments";
    private String email = "bob@mail.com";
    private String input = "QUIT";
    private String[] cmdArgs = {"QUIT"};
    private State state = State.AUTHORIZATION;
    private Database mockedDatabase = mock(Database.class);
    private Command command = new CommandQUIT(mockedDatabase);

    public TestCommandQUIT(){
        when(mockedDatabase.getNumberOfMessages(email, true)).thenAnswer(new Answer() {
            private int count = 1;

            public Object answer(InvocationOnMock invocation) {
                if (count-- == 1)
                    return 1;

                return 0;
            }
        });
        when(mockedDatabase.deleteMarkedMails(email)).thenReturn(1);
    }
    @Test
    public void testCommandQUITIncorrectNumArgs(){
        cmdArgs = new String[]{"QUIT", "1", "d"};
        assertEquals(INCORRECT_NUM_ARGS + input, command.execute(input, cmdArgs, state, email));
    }

    @Test
    public void testCommandQUITOk(){
        cmdArgs = new String[]{"QUIT"};
        assertEquals(QUIT_OK + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandQUITOkUpdate(){
        state = State.TRANSACTION;
        assertEquals("+OK 1 messages deleted" + input, command.execute(input, cmdArgs, state, email));
    }
    @Test
    public void testCommandQUITOkUpdateError(){
        when(mockedDatabase.getNumberOfMessages(email, true)).thenAnswer(new Answer() {
            private int count = 1;

            public Object answer(InvocationOnMock invocation) {
                if (count-- == 1)
                    return 1;

                return 2;
            }
        });
        state = State.TRANSACTION;
        assertEquals(QUIT_ERROR + input, command.execute(input, cmdArgs, state, email));
    }
}