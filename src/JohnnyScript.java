import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Compiles JohnnyScript (.jns) files to ram files for the Johnny Simulator
 */
public class JohnnyScript {

    public static void main(String[] args) {
        String filename = "";
        try {
            filename = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No argument given.\nUsage: java JohnnyScript filename");
            System.exit(1);
        }

        Path path = FileSystems.getDefault().getPath(filename);

        if (!Files.isReadable(path)) {
            System.out.println("Invalid Filename: " + filename);
            System.exit(1);
        }

    }

}
