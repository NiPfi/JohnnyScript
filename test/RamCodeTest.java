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

    @Test
    public void testAddCode() throws Exception {
        RamCode code = new RamCode();
        code.addCode(JohnnyScript.Codes.ADD.codeOrdinal + "001");
        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(0));
        assertEquals(JohnnyScript.Codes.ADD.codeOrdinal + "001",codeList.get(1));

        for (int i = 2; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }

    @Test
    public void testAddVar() throws Exception {
        RamCode code = new RamCode();
        code.addVar("tst",5);

        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "002",codeList.get(0));
        assertEquals("005",codeList.get(1));

        for (int i = 2; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }

    @Test(expected = VariableNotInitializedException.class)
    public void testAddVarRefWithoutInit() throws Exception {
        RamCode code = new RamCode();
        code.addCodeWithVar(JohnnyScript.Codes.ADD.codeOrdinal+" #tst");
    }

    @Test(expected = DuplicateVariableException.class)
    public void testAddDuplicateVar() throws Exception {
        RamCode code = new RamCode();
        code.addVar("tst", 5);
        code.addVar("tst", 8);
    }

    @Test
    public void testAddVarRef() throws Exception {
        RamCode code = new RamCode();
        code.addVar("tst",5);
        code.addCodeWithVar(JohnnyScript.Codes.ADD.codeOrdinal+" #tst");

        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "002",codeList.get(0));
        assertEquals("005",codeList.get(1));
        assertEquals(JohnnyScript.Codes.ADD.codeOrdinal+"001",codeList.get(2));

        for (int i = 3; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }

    @Test
    public void testAddJumpAfterJumpPoint() throws Exception {
        RamCode code = new RamCode();
        code.addJumpPoint("tst");
        code.addJump("tst");

        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(0));
        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(1));

        for (int i = 2; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }

    @Test
    public void testAddJumpBeforeJumpPoint() throws Exception {
        RamCode code = new RamCode();
        code.addJump("tst");
        code.addJumpPoint("tst");

        List codeList = code.getCode();

        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(0));
        assertEquals(JohnnyScript.Codes.JMP.codeOrdinal + "001",codeList.get(1));

        for (int i = 2; i < 1000; i++) {
            assertEquals("000",codeList.get(i));
        }
    }

    @Test(expected = DuplicateJumpPointException.class)
    public void testAddDuplicateJumpPoint() throws Exception {
        RamCode code = new RamCode();
        code.addJumpPoint("tst");
        code.addJumpPoint("tst");
    }

    @Test(expected = InvalidJumpsException.class)
    public void testInvalidJump() throws Exception {
        RamCode code = new RamCode();
        code.addJump(JohnnyScript.Codes.JMP.codeOrdinal + " tst");

        code.getCode();
    }

    @Test
    public void testProgram() throws Exception {
        RamCode code = new RamCode();
        code.addVar("z3", 0);
        code.addCodeWithVar(JohnnyScript.Codes.NULL.codeOrdinal + "#z3");
        code.addJumpPoint("start");
        code.addCodeWithVar(JohnnyScript.Codes.TAKE.codeOrdinal + "#z3");
        code.addVar("z1", 5);
        code.addCodeWithVar(JohnnyScript.Codes.ADD.codeOrdinal + "#z1");
        code.addVar("z2", 3);
        code.addCodeWithVar(JohnnyScript.Codes.SAVE.codeOrdinal + "#z2");
        code.addCodeWithVar(JohnnyScript.Codes.DEC.codeOrdinal + "#z1");
        code.addCodeWithVar(JohnnyScript.Codes.TST.codeOrdinal + "#z1");
        code.addJump("start");
        code.addCode(JohnnyScript.Codes.HLT.codeOrdinal + "0");
    }
}