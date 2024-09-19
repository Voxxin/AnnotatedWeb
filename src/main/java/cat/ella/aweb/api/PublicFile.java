package cat.ella.aweb.api;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PublicFile {

    private final Map<String, byte[]> pathAndData = new HashMap<>();

    public Collection<String> getPaths() {
        return pathAndData.keySet();
    }

    public byte[] getData(String path) {
        return pathAndData.get(path);
    }

    /**
     * Handles directory inclusion based on specified parameters.
     *
     * @param localPath   The local file system path.
     * @param publicPath  The path to be exposed publicly.
     * @param pathType    Specifies the type of path:
     *                    0 - Internal
     *                    1 - External
     * @param publicDepth Specifies what to capture within the specified path (if directory):
     *                    0 - All items within the directory
     *                    1 - All items within the directory, including subdirectories
     */
    public PublicFile(Class<?> callingClass, String localPath, String publicPath, int pathType, int publicDepth) {
        callingClass = callingClass == null ? this.getClass() : callingClass;
        localPath = !localPath.contains(".") && (localPath.endsWith("/") || localPath.endsWith("\\")) ? localPath : localPath + "/";
        publicPath = publicPath.endsWith("/") ? publicPath : publicPath + "/";

        Collection<String> paths = captureFilePaths(callingClass, localPath, pathType);
        handleCapturedFiles(callingClass, paths, localPath, publicPath, pathType, publicDepth);
    }

    private Collection<String> captureFilePaths(Class<?> callingClass, String localPath, int pathType) {
        Set<String> paths = new HashSet<>();
        try {
            switch (pathType) {
                case 0:
                    final File jarFile = new File(callingClass.getProtectionDomain().getCodeSource().getLocation().getPath());
                    if (jarFile.isFile()) { // Running from JAR
                        JarFile jar = new JarFile(jarFile);
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.isDirectory() || !entry.getName().startsWith(localPath)) continue;
                            paths.add(entry.getName());
                        }
                    } else { // Running from IDEs
                        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(localPath);
                        if (inputStream != null) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                reader.lines().forEach(p -> paths.addAll(p.contains(".") ?
                                        List.of(localPath + p) : captureFilePaths(callingClass, localPath + p + "/", 0)));
                            }
                        }
                    }
                    break;
                case 1:
                    Files.walk(Paths.get(localPath))
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .forEach(path -> {
                                String formattedP = path.replaceAll("^.*" + localPath, localPath + path);
                                paths.add(formattedP);
                            });
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    private void handleCapturedFiles(Class<?> callingClass, Collection<String> paths, String localPath, String publicPath, int pathType, int publicDepth) {
        for (String path : paths) {
            String formattedP = path.replaceAll("^.*" + localPath, publicPath);
            formattedP = formattedP.startsWith("/") ? formattedP.replaceFirst("^/", "") : formattedP;
            if (publicDepth == 0 && (formattedP.contains("/") || formattedP.contains("\\"))) continue;
            if (formattedP.endsWith("/") || formattedP.endsWith("\\")) continue;

            if (pathType == 0) pathAndData.putIfAbsent(publicPath + formattedP, getFileBytes(callingClass, path));
            else {
                try {
                    pathAndData.putIfAbsent(publicPath + formattedP, Files.readAllBytes(new File(path).toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public byte[] getFileBytes(Class<?> callingClass, String filePath) {
        final File jarFile = new File(callingClass.getProtectionDomain().getCodeSource().getLocation().getPath());
        byte[] bytes = null;
        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String path = entry.getName();
                    if (path.equals(filePath)) {
                        File file = File.createTempFile("tempFile", "." + path.replaceAll("^.*\\.", ""));

                        try (InputStream inputStream = jar.getInputStream(entry)) {
                            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        bytes = Files.readAllBytes(file.toPath());
                        file.delete();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading JAR file: " + e.getMessage());
            }
        } else {
            try {
                URL url = callingClass.getProtectionDomain().getClassLoader().getResource(filePath);
                if (url == null) return null;
                File file = new File(url.toURI());
                bytes = Files.readAllBytes(file.toPath());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return bytes;
    }

}
