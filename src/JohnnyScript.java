import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ResourceBundle;

/**
 * Compiles JohnnyScript (.jns) files to ram files for the Johnny Simulator
 */
public class JohnnyScript {

    private static final String OUTPUT_EXTENSION = ".ram";

    public static void main(String[] args) throws IOException {

        String filename = getFilename(args);
        Path output = generateOutputfile(filename);

    }

    /**
     * Generates the output file named like the input file
     * @param filename Name of the input file to base the output file on (file ending will be stripped)
     * @return {@link java.nio.file.Path} object for the output file
     * @throws IOException if filesystem error
     */
    private static Path generateOutputfile(String filename) throws IOException {
        String name = filename.substring(0, filename.indexOf('.'));
        String outputFile = name + OUTPUT_EXTENSION;
        Path outputPath = FileSystems.getDefault().getPath(outputFile);
        Files.createFile(outputPath);
        return outputPath;
    }

    /**
     * Checks the first argument for a filename and returns it if present and readable
     *
     * @param args String array of program arguments
     * @return Filename given to the program
     * @throws IllegalArgumentException if false argument is given
     */
    private static String getFilename(String[] args) {
        String filename;
        try {
            filename = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            String test = "test";
            System.err.println("No argument given.\nUsage: java JohnnyScript filename");
            throw new IllegalArgumentException();
        }

        checkInputFile(filename);

        return filename;
    }

    /**
     * Checks if the given filename is for a readable file that can be used to compile
     *
     * @param filename String with the name of a file to check
     * @throws IllegalArgumentException if file for given filename isn't readable
     */
    private static void checkInputFile(String filename) {
        Path path = FileSystems.getDefault().getPath(filename);

        if (!Files.isReadable(path)) {
            System.err.println("Invalid Filename: " + filename);
            throw new IllegalArgumentException();
        }
    }

}
