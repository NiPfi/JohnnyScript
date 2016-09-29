import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles JohnnyScript (.jns) files to ram files for the Johnny Simulator
 */
public class JohnnyScript {

    private static final String OUTPUT_EXTENSION = ".ram";
    private static final String LINE_COMMENT_DELIMITER = "//";

    public static void main(String[] args) throws IOException {

        Path source = getFilename(args);
        List<String> code = Files.readAllLines(source);
        writeOutFile(source.getFileName().toString(), compileCode(code));

    }

    private static List<String> compileCode(List<String> lines) {
        List<String> compiled = new ArrayList<>();

        for (String line: lines) {
            String compiledLine = compile(line);
            if (!compiledLine.equals("")) {
                compiled.add(compiledLine);
            }
        }

        return compiled;
    }

    private static String compile(String line) {
        if (line.contains(LINE_COMMENT_DELIMITER)) {
            int commentStart = line.indexOf(LINE_COMMENT_DELIMITER);
            String nonComment = line.substring(0, commentStart);
            return nonComment.trim();
        }
        else
            return line.trim();
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

}
