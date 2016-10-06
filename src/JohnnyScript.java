import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiles JohnnyScript (.jns) files to ram files for the Johnny Simulator
 */
public class JohnnyScript {

    private static final String OUTPUT_EXTENSION = ".ram";
    private static final String LINE_COMMENT_DELIMITER = "//";
    private static final String JUMP_POINT_DELIMITER = ":";
    private static final String VARIABLE_DELIMITER = "#";

    public static void main(String[] args) throws IOException {

        Path source = getFilename(args);
        List<String> code = Files.readAllLines(source);
        try {
            writeOutFile(source.getFileName().toString(), compileCode(code));
        } catch (Exception e) {
            throw new CompilerHaltException("Compiler halted due to erroneous code!\n", e);
        }

    }

    /**
     * Generates ram code using RamCode object by iterating through every line of source code and handling it according to it's content
     *
     * @param sourceLines {@link List} of String objects containing lines of JohnnyScript code
     * @return compiled numeric code for .ram file
     */
    private static List<String> compileCode(List<String> sourceLines) throws InvalidScriptException, DuplicateVariableException, VariableNotInitializedException, DuplicateJumpPointException {
        RamCode code = new RamCode();
        int lineNumber = 0;
        for (String line:sourceLines) {
            line = decomment(line);
            if (line == null) {
                continue;
            }
            if (line.contains(VARIABLE_DELIMITER)) {
                code = handleVariable(code, lineNumber, line);
            } else {
                if (line.contains(JUMP_POINT_DELIMITER)) {
                    // handle jump point
                    String jpName = line.replace(":", "");
                    code.addJumpPoint(jpName);
                } else {
                    // check if jump, if yes handle accordingly
                    String[] parts = line.split(" ");
                    if(Codes.valueOf(parts[0].toUpperCase()).codeOrdinal == Codes.JMP.codeOrdinal) {
                        code.addJump(parts[1]);
                    } else code.addCode(encode(line, true)); // else just add the code as is
                }

            }
            lineNumber++;

        }

        try {
            return code.getCode();
        } catch (InvalidJumpsException e) {
            throw new CompilerHaltException("Compiler halted due to erroneous code!\n", e);
        }
    }

    /**
     * Handles the case where the compiler encounters a line containing a variable. If the delimiter is at the beginning
     * of the line, the line is for initializing a new variable. If the delimiter is preceded by some String, that string
     * is parsed as instruction and the variable will be resolved to the address it has been initialized at
     *
     * @param code RamCode object containing the output code
     * @param lineNumber the line the variable is declared/used at
     * @param line the line of source code the variable is gathered from
     * @return RamCode object with added variable
     * @throws InvalidScriptException on syntax error
     * @throws DuplicateVariableException on creating a variable that's already been initialized
     * @throws VariableNotInitializedException on accessing an inexistent variable
     */
    private static RamCode handleVariable(RamCode code, int lineNumber, String line) throws InvalidScriptException, DuplicateVariableException, VariableNotInitializedException {
        String[] parts = line.split(VARIABLE_DELIMITER + "| ");
        if (parts[0].length() == 0) {
            // variable declaration (# at start of line)
            if(parts.length != 3) {
                throw new InvalidScriptException("Syntax error at line " + lineNumber + ": " + line + " (variable declaration: #varname [int])");
            } else code.addVar(parts[1],Integer.valueOf(parts[2]));
        } else {
            // reference to variable
            if(parts.length != 3) {
                throw new InvalidScriptException("Syntax error at line " + lineNumber + ": " + line + " (variable declaration: #varname [int])");
            } else code.addCodeWithVar(encode(parts[0], false),parts[2]);
        }
        return code;
    }

    /**
     * Removes comments from line and trims excess whitespace
     *
     * @param line Line of JohnnyScript code
     * @return compiled line of .ram code or null if line is a comment
     */
    private static String decomment(String line) {
        if (line.contains(LINE_COMMENT_DELIMITER)) {
            /* Separate code from comment */
            int commentStart = line.indexOf(LINE_COMMENT_DELIMITER);
            String nonComment = line.substring(0, commentStart);
            if (!nonComment.equals(""))
                return nonComment.trim();
            else return null;
        } else return line.trim();
    }

    /**
     * Converts textual instruction with address to numeric instruction with address. The address always consists out of three digits.
     *
     * @param line contains instruction with address or variable
     * @param appendLow when true and a single part instruction (without address) is given, the address 000 is appended
     * @return numeric line for instruction
     * @throws InvalidScriptException on invalid JohnnyScript code
     */
    private static String encode(String line, boolean appendLow) throws InvalidScriptException {
        String[] parts = line.split(" ");
        if (parts.length > 2) throw new InvalidScriptException("Syntax error (too many parts): " + line);
        if (parts.length == 1) {
            String output = Codes.valueOf(parts[0].toUpperCase()).getCode();
            if (appendLow) output = output + "000";
            return output;
        } else {
            String code = parts[0].toUpperCase();
            String hi = Codes.valueOf(code).getCode();
            String lo = String.format("%03d", Integer.parseInt(parts[1]));
            return hi + lo;
        }

    }

    /**
     * Generates the output file named like the input file
     *
     * @param filename Name of the input file to base the output file on (file ending will be stripped)
     * @throws IOException if filesystem error
     */
    private static void writeOutFile(String filename, List<String> lines) throws IOException {
        String name = filename.substring(0, filename.indexOf('.'));
        String outputFile = name + OUTPUT_EXTENSION;
        Path outputPath = FileSystems.getDefault().getPath(outputFile);
        Files.write(outputPath, lines);
    }

    /**
     * Checks the first argument for a filename and returns it if present and readable
     *
     * @param args String array of program arguments
     * @return Path to the file from arguments
     * @throws IllegalArgumentException if false argument is given
     */
    private static Path getFilename(String[] args) {
        String filename;
        try {
            filename = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            String test = "test";
            System.err.println("No argument given.\nUsage: java JohnnyScript filename");
            throw new IllegalArgumentException();
        }

        Path path = FileSystems.getDefault().getPath(filename);

        checkInputFile(path);

        return path;
    }

    /**
     * Checks if the given filename is for a readable file that can be used to compile
     *
     * @param path Path of a file to check
     * @throws IllegalArgumentException if file for given filename isn't readable
     */
    private static void checkInputFile(Path path) {
        if (!Files.isReadable(path)) {
            System.err.println("Invalid Filename: " + path.getFileName().toString());
            throw new IllegalArgumentException();
        }
    }

    enum Codes {
        TAKE(1), ADD(2), SUB(3), SAVE(4), JMP(5), TST(6), INC(7), DEC(8), NULL(9), HLT(10);

        int codeOrdinal = 0;

        Codes(int ord) {
            this.codeOrdinal = ord;
        }

        public String getCode() {
            return String.valueOf(codeOrdinal);
        }
    }

}

/**
 * Gathers all  compiled code, variables and jump points and links them accordingly. It keeps the code in a list that is
 * being filled procedurally by parsing the source code file.
 */
class RamCode {

    private static final int MAX_LINES = 999;

    private static int writeIndex; // keeps track of the current line

    private ArrayList<String> code;
    private Map<String, Integer> variables;
    private Map<String, Integer> varLoc;
    private Map<String, Integer> jumpPoints;
    private Map<String, List<Integer>> jumps;

    /**
     * Constructor initializes class variables
     */
    RamCode() {
        code = new ArrayList<>();
        variables = new LinkedHashMap<>();
        varLoc = new LinkedHashMap<>();
        jumpPoints = new LinkedHashMap<>();
        jumps = new LinkedHashMap<>();
    }

    /**
     * Adds a code to the list of codes
     * @param input numeric ram code
     */
    void addCode(String input) {
        code.add(input);
    }

    /**
     * Adds a variable to the map of variables and keeps track of the line where the variable will be written to
     * @param name Name of the variable
     * @param value The value it's being initialized to
     * @throws DuplicateVariableException on attempting to initialize a variable with the name of a pre-existing one
     */
    void addVar(String name, int value) throws DuplicateVariableException {
        if (variables.containsKey(name)) {
            throw new DuplicateVariableException("Variable cannot be defined twice: " + name);
        } else {
            variables.put(name, value);
            varLoc.put(name, variables.size());
        }
    }

    /**
     * Combines the instruction with the address the variable with the given name is stored at
     * @param instruction Any valid instruction
     * @param var Any initialized variable
     * @throws VariableNotInitializedException if the variable has not been defined beforehand
     */
    void addCodeWithVar(String instruction, String var) throws VariableNotInitializedException {
        if (!variables.containsKey(var)) {
            throw new VariableNotInitializedException("Variable has not been initialized: " + var);
        } else {
            int line = varLoc.get(var);
            code.add(instruction + String.format("%03d", line));
        }
    }

    /**
     * Defines a jump point at the current location in the code list
     * @param jpName the name of the jump point
     * @throws DuplicateJumpPointException on attempting to create a second variable with the same name
     */
    void addJumpPoint(String jpName) throws DuplicateJumpPointException {
        if (jumpPoints.containsKey(jpName)) {
            throw new DuplicateJumpPointException("Same jump point can't be set twice: " + jpName);
        } else {
            jumpPoints.put(jpName, code.size());
        }
    }

    /**
     * Creates a placeholder for a jump to a jump point in the code list. This can only be done after parsing all other
     * code because while parsing the source the compiler might encounter a variable which causes all addresses to be shifted down by 1
     *
     * The map uses a List to store all jumps so that multiple jumps can be stored for the same jump point.
     *
     * @param jpName Jump point this jump will be linked to
     */
    void addJump(String jpName) {
        List<Integer> jumpLines;
        if (jumps.containsKey(jpName)) {
            jumpLines = jumps.get(jpName);
        } else {
            jumpLines = new ArrayList<>();
        }
        jumpLines.add(code.size());
        jumps.put(jpName,jumpLines);

        code.add(jpName + ":");
    }

    /**
     * Initializes a List of Strings by putting in the empty address "000" until MAX_LINES is reached
     */
    private List<String> initializeZeros(List<String> code) {
        for (int i = 0; i <= MAX_LINES; i++) {
            code.add("000");
        }
        return code;
    }

    /**
     * Generates the ram file in its final form. Line 0 contains a jump to the first line after all variables.
     * Then all variables are placed with the value they are supposed to be initialized to.
     * Afterwards the code from the code list is inserted with placeholders for the jumps.
     * Lastly the placeholders are replaced by their corresponding jump instruction
     *
     * @return ram file
     * @throws InvalidJumpsException if there is a jump instruction for a jump point that has not been defined
     */
    List<String> getCode() throws InvalidJumpsException {
        List<String> output = new ArrayList<>();
        initializeZeros(output);

        writeIndex = 0;

        // generate line 0 (jump to first instruction)
        output.set(writeIndex, generateLineZero());
        writeIndex++;

        // lambda instruction that prints every variable in the map formatted as 3 digits
        variables.forEach((k, v) -> {
            output.set(writeIndex, String.format("%03d", v));
            writeIndex++;
        });

        // loop that appends all code from the code list to the variables
        for (String loc : code
                ) {
            output.set(writeIndex, loc);
            writeIndex++;
        }

        List<String> invalid = new ArrayList<>();

        // lambda instruction that replaces each jump with the jmp instruction and the adress the according jump point is located at
        jumps.forEach((jpName,jumpList) -> {
            if(!jumpPoints.containsKey(jpName)) {
                invalid.add(jpName);
            } else {
                for (int line:jumpList) {
                    line = 1 + variables.size() + line;
                    assert output.get(line).equals(jpName + ":");
                    output.set(line, JohnnyScript.Codes.JMP.codeOrdinal + String.format("%03d", 1+variables.size()+jumpPoints.get(jpName)));
                }
            }
        });

        if (!invalid.isEmpty()) {
            throw new InvalidJumpsException("Jumps to inexistent jump points: " + invalid.toString());
        }

        assert writeIndex == 1 + variables.size() + code.size();

        return output;
    }

    /**
     * Genereates the first line in the output file that points to the first line after all variables
     * @return ram code with jump to first line
     */
    private String generateLineZero() {
        String firstLocAdress = String.format("%03d", variables.size() + 1);
        return JohnnyScript.Codes.JMP.codeOrdinal + firstLocAdress;
    }
}

class InvalidScriptException extends Exception {

    InvalidScriptException(String message) {
        super(message);
    }
}

class CompilerHaltException extends RuntimeException {

    CompilerHaltException(String message, Throwable cause) {
        super(message, cause);
    }
}

class DuplicateVariableException extends Exception {

    DuplicateVariableException(String message) {
        super(message);
    }
}

class VariableNotInitializedException extends Exception {

    VariableNotInitializedException(String message) {
        super(message);
    }
}

class DuplicateJumpPointException extends Exception {
    DuplicateJumpPointException(String message) {
        super(message);
    }
}

class InvalidJumpsException extends Exception {
    InvalidJumpsException(String message) {
        super(message);
    }
}
