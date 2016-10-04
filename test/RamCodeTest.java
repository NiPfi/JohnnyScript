import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for RamCode object
 */
public class RamCodeTest {

    @Test
    public void testInitialize() throws Exception {
        RamCode code = new RamCode();
        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(0));

        for (int i = 1; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }
}