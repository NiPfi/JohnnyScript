import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Testing class for JohnnyScript
 */
public class JohnnyScriptTest {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        System.setErr(new PrintStream(errContent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void argumentMissing() throws Exception {
        JohnnyScript.main(new String[]{});
        assertEquals("No argument given.\nUsage: java JohnnyScript filename",errContent.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFilename() throws Exception {
        String filename = "invalid";
        JohnnyScript.main(new String[]{filename});
        assertEquals("Invalid Filename: " + filename, errContent.toString());
    }

}