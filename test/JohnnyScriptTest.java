import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testing class for JohnnyScript
 */
public class JohnnyScriptTest {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();


    // Valid file for testing
    private final String validFilename = "jUnit";

    private final String validExtension = ".jns";
    private final String validFile = validFilename + validExtension;
    private final Path inputPath = FileSystems.getDefault().getPath(validFile);

    private final String validOutputExtension = ".ram";
    private final String outputFile = validFilename + validOutputExtension;
    private final Path outputPath = FileSystems.getDefault().getPath(outputFile);

    @Before
    public void setUp() throws Exception {
        System.setErr(new PrintStream(errContent));

        Files.createFile(inputPath);
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(inputPath);
        Files.deleteIfExists(outputPath);
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

    @Test
    public void validInput() throws Exception {

        JohnnyScript.main(new String[]{validFile});

        assertTrue("Output file not generated or incorrectly named", Files.exists(outputPath));

    }

    @Test
    public void commentLine() throws Exception {
        List<String> testCode = new ArrayList<>();
        testCode.add("add 0");
        testCode.add("//Test comment");
        testCode.add("add 0");
        Files.write(inputPath, testCode);

        JohnnyScript.main(new String[]{validFile});

        assertTrue("Output file not generated or incorrectly named", Files.exists(outputPath));

        List outLines = Files.readAllLines(outputPath);

        testCode = new ArrayList<>();
        testCode.add("2000");
        testCode.add("2000");

        assertEquals(1000, outLines.size());
        for (int i = 0; i < testCode.size(); i++) {
            assertEquals("Line " + i, testCode.get(i),outLines.get(i));
        }

    }

    @Test
    public void commentInline() throws Exception {
        List<String> testCode = new ArrayList<>();
        testCode.add("add 0");
        testCode.add("add 1 //Test comment");
        testCode.add("add 0");
        Files.write(inputPath, testCode);

        JohnnyScript.main(new String[]{validFile});

        assertTrue("Output file not generated or incorrectly named", Files.exists(outputPath));

        List outLines = Files.readAllLines(outputPath);

        testCode = new ArrayList<>();
        testCode.add("2000");
        testCode.add("2001");
        testCode.add("2000");

        assertEquals(1000, outLines.size());
        for (int i = 0; i < testCode.size(); i++) {
            assertEquals("Line " + i, testCode.get(i),outLines.get(i));
        }

    }

    //TODO Tests for every code

}