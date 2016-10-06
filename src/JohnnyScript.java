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
     * Generates ram code using RamCode object
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
            } else {
                if (line.contains(JUMP_POINT_DELIMITER)) {
                    String jpName = line.replace(":", "");
                    code.addJumpPoint(jpName);
                } else {
                    String[] parts = line.split(" ");
                    if(Codes.valueOf(parts[0].toUpperCase()).codeOrdinal == Codes.JMP.codeOrdinal) {
                        code.addJump(parts[1]);
                    } else code.addCode(encode(line, true));
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
     * Converts textual instruction with address to numeric instruction with address
     *
     * @param line contains instruction with address or variable
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

class RamCode {

    private static final int MAX_LINES = 999;

    private static int writeIndex;

    private ArrayList<String> code;
    private Map<String, Integer> variables;
    private Map<String, Integer> varLoc;
    private Map<String, Integer> jumpPoints;
    private Map<String, List<Integer>> jumps;

    RamCode() {
        code = new ArrayList<>();
        variables = new LinkedHashMap<>();
        varLoc = new LinkedHashMap<>();
        jumpPoints = new LinkedHashMap<>();
        jumps = new LinkedHashMap<>();
    }

    void addCode(String input) {
        code.add(input);
    }

    void addVar(String name, int value) throws DuplicateVariableException {
        if (variables.containsKey(name)) {
            throw new DuplicateVariableException("Variable cannot be defined twice: " + name);
        } else {
            variables.put(name, value);
            varLoc.put(name, variables.size());
        }
    }

    void addCodeWithVar(String commandOrdinal, String var) throws VariableNotInitializedException {
        if (!variables.containsKey(var)) {
            throw new VariableNotInitializedException("Variable has not been initialized: " + var);
        } else {
            int line = varLoc.get(var);
            code.add(commandOrdinal + String.format("%03d", line));
        }
    }

    void addJumpPoint(String jpName) throws DuplicateJumpPointException {
        if (jumpPoints.containsKey(jpName)) {
            throw new DuplicateJumpPointException("Same jump point can't be set twice: " + jpName);
        } else {
            jumpPoints.put(jpName, code.size());
        }
    }

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

    private List<String> initializeZeros(List<String> code) {
        for (int i = 0; i <= MAX_LINES; i++) {
            code.add("000");
        }
        return code;
    }

    List<String> getCode() throws InvalidJumpsException {
        List<String> output = new ArrayList<>();
        initializeZeros(output);

        writeIndex = 0;

        output.set(writeIndex, generateLineZero());
        writeIndex++;

        variables.forEach((k, v) -> {
            output.set(writeIndex, String.format("%03d", v));
            writeIndex++;
        });

        for (String loc : code
                ) {
            output.set(writeIndex, loc);
            writeIndex++;
        }

        List<String> invalid = new ArrayList<>();

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
