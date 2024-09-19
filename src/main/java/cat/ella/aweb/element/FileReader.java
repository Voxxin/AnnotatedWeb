package cat.ella.aweb.element;

import java.io.IOException;
import java.io.InputStream;

public class FileReader {

    public static byte[] read(Class<?> callingClass, String path) {
        try (InputStream inputStream = callingClass.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + path.substring(path.lastIndexOf("/") + 1));
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file at path: " + path, e);
        }
    }
}
