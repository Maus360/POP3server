import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDatabase {
    @Test
    public void testconnection(){
        Database database = new Database();

        assertEquals(5, database.myClass(9, 4));
        assertEquals(40, database.myClass(9, 4));
    }
}
