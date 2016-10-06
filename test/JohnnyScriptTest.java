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

        List<String> outLines = Files.readAllLines(outputPath);

        testCode = new ArrayList<>();
        testCode.add("5001");
        testCode.add("2000");
        testCode.add("2000");

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

        List<String> outLines = Files.readAllLines(outputPath);

        testCode = new ArrayList<>();
        testCode.add("5001");
        testCode.add("2000");
        testCode.add("2001");
        testCode.add("2000");

        for (int i = 0; i < testCode.size(); i++) {
            assertEquals("Line " + i, testCode.get(i),outLines.get(i));
        }

    }

    @Test
    public void testMaxLines() throws Exception {
        List<String> testCode = new ArrayList<>();
        Files.write(inputPath, testCode);

        JohnnyScript.main(new String[]{validFile});
        List<String> outLines = Files.readAllLines(outputPath);
        assertEquals(1000, outLines.size());
    }

    @Test
    public void testTake() throws Exception {
        exhTest("TAKE");
    }
    @Test
    public void testAdd() throws Exception {
        exhTest("ADD");
    }
    @Test
    public void testSub() throws Exception {
        exhTest("SUB");
    }
    @Test
    public void testSave() throws Exception {
        exhTest("SAVE");
    }
    @Test
    public void testTst() throws Exception {
        exhTest("TST");
    }
    @Test
    public void testInc() throws Exception {
        exhTest("INC");
    }
    @Test
    public void testDec() throws Exception {
        exhTest("DEC");
    }
    @Test
    public void testNull() throws Exception {
        exhTest("NULL");
    }
    @Test
    public void testHlt() throws Exception {
        exhTest("HLT");
    }

    /**
     * Tests conversion of all possible addresses for a code
     * @param code Code to test
     */
    private void exhTest(String code) throws Exception {
        for (int i = 0; i < 999; i++) {
            List<String> testCode = new ArrayList<>();
            testCode.add(code + " " + i);
            Files.write(inputPath, testCode);

            JohnnyScript.main(new String[]{validFile});

            List<String> outLines = Files.readAllLines(outputPath);

            testCode = new ArrayList<>();
            testCode.add(JohnnyScript.Codes.valueOf(code).getCode()+ String.format("%03d",i));

            assertEquals("Line " + 0, "5001",outLines.get(0));

            for (int j = 1; j < testCode.size(); j++) {
                assertEquals("Line " + j, testCode.get(j),outLines.get(j));
            }
        }
    }
}