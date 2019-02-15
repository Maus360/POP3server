import static org.junit.Assert.assertEquals;

import Model.Database;
import org.junit.Test;

import java.sql.SQLException;

public class TestDatabase {
    private Database database = new Database();
    @Test(expected = SQLException.class)
    public void testUserNotExists(){
        database.userExists("nonexistent@mail.com");
    }

    @Test
    public void testUserExists(){
        assertEquals(true, database.userExists("admin@mail.com"));
        assertEquals(false, database.userExists("chel@mail.com"));
    }

    @Test
    public void testGetMailDropSize(){
        assertEquals(81, database.getMaildropSize("bob@mail.com"));
    }

    @Test
    public void testPasswordCorrect(){
        assertEquals(true, database.passwordCorrect("bob@mail.com", "hellobob"));
        assertEquals(false, database.passwordCorrect("admin@mail.com", "admin"));
    }

    @Test
    public void testGetNumberOfMessages(){
        assertEquals(1, database.getNumberOfMessages("bob@mail.com", false));
    }



}
