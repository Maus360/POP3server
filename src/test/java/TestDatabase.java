import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDatabase {
    @Test
    public void testconnection(){
        Database database = new Database();

        assertEquals(true, database.userExists("admin@mail.com"));
        assertEquals(false, database.userExists("chel@mail.com"));
    }
}
