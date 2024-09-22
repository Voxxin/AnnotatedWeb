package cat.ella.aweb;

import cat.ella.aweb.annotations.Capture;
import cat.ella.aweb.annotations.Route;
import cat.ella.aweb.annotations.WebService;
import cat.ella.aweb.api.PublicFile;
import cat.ella.aweb.api.FormattedRequest;
import cat.ella.aweb.api.FormattedResponse;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.*;
import java.lang.reflect.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WebServer {
    private final int port;
    private Method useOnFailure = null;
    private final ArrayList<Method> openRoutes = new ArrayList<>();
    private final ArrayList<Method> captrueRoutes = new ArrayList<>();
    private final ArrayList<PublicFile> publicFiles = new ArrayList<>();
    private ServerSocket webServer;
    private Thread webServerThread;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() {
        webServerThread = new Thread(() -> {
            try {
                System.out.println("Server started on port " + port+"...");
                AnnotationHandler();

                if (this.webServer == null) this.webServer = new ServerSocket(port);
                while (true) {
                    Socket clientSocket = this.webServer.accept();
                    FormattedRequest formattedRequest = getFormattedRequest(clientSocket);
                    if (formattedRequest == null) continue;

                    if (useOnFailure == null) this.useOnFailure = this.openRoutes.stream().filter(r -> r.getAnnotation(Route.class).errorRoute()).findFirst().orElseGet(() -> null);

                    this.publicFiles.stream()
                            .filter(r -> r.getPaths().contains(formattedRequest.getPath()))
                            .findAny()
                            .ifPresent(r -> handlePublicFile(formattedRequest, r.getData(formattedRequest.getPath()), clientSocket));
                    this.openRoutes.stream()
                            .filter(r -> r.getAnnotation(Route.class).path().equals(formattedRequest.getPath()))
                            .findAny()
                            .ifPresent(r -> handleRoute(r, formattedRequest, clientSocket));

                    this.captrueRoutes.stream()
                            .filter(r -> r.getAnnotation(Route.class).path().equals(formattedRequest.getPath()) && r.getAnnotation(Capture.class).method().equals(formattedRequest.getMethod()))
                            .findAny()
                            .ifPresent(r -> handleCapture(r, formattedRequest));

                    if (clientSocket.isClosed()) continue;
                    if (useOnFailure != null) handleRoute(useOnFailure, formattedRequest, clientSocket);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        webServerThread.start();
    }

    private static FormattedRequest getFormattedRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

        List<String> headers = new ArrayList<>();
        String line;
        int contentLength = -1;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            headers.add(line);
            if (line.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(line.substring(16).trim());
            }
        }

        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer);
            headers.add(String.valueOf(buffer));
        }

        return !headers.isEmpty() ? new FormattedRequest(headers) : null;
    }

    public void stop() {
        webServerThread.interrupt();
        System.out.println("Server(" + port + ") stopped.");
    }

    void AnnotationHandler() {
        Reflections massReflections = new Reflections(new ConfigurationBuilder().forPackage("").setScanners(Scanners.MethodsAnnotated));

        for (Method method : massReflections.getMethodsAnnotatedWith(WebService.class)) {
            if (method.isAnnotationPresent(Route.class)
                    && Arrays.stream(method.getParameters()).allMatch(p -> FormattedRequest.class.isAssignableFrom(p.getType()))
                    && byte[].class.isAssignableFrom(method.getReturnType())
                    && !method.isAnnotationPresent(Capture.class)) {
                openRoutes.add(method);
            } else if (method.isAnnotationPresent(Route.class) && method.isAnnotationPresent(Capture.class)) {
                captrueRoutes.add(method);
            }
        }
    }

    public void add(PublicFile file) {
        publicFiles.add(file);
    }

    private void handleRoute(Method method, FormattedRequest request, Socket clientSocket) {
        if (clientSocket.isClosed()) return;
        try (OutputStream outputStream = clientSocket.getOutputStream()) {
            byte[] response = (byte[]) method.invoke(method.getDeclaringClass(), request);
            byte[] dataToSend = (response != null && response.length > 0)
                    ? response
                    : new FormattedResponse().contentType("application/json").statusCode(404).statusMessage("Not Found").build();
            outputStream.write(dataToSend);
            clientSocket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCapture(Method method, FormattedRequest request) {
        try {
            method.invoke(method.getDeclaringClass(), request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePublicFile(FormattedRequest request, byte[] fileData, Socket clientSocket) {
        System.out.println(request.getPath());
        System.out.println(clientSocket.isClosed());

        if (clientSocket.isClosed()) return;
        try {
            String pathType = request.getPath().substring(request.getPath().lastIndexOf(".") + 1);
            OutputStream outputStream = clientSocket.getOutputStream();

            Path tempFile = Files.createTempFile("tempWebconfig", "." + pathType);
            Files.write(tempFile, fileData);
            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                outputStream.write(new FormattedResponse()
                        .contentType(Files.probeContentType(tempFile))
                        .content(inputStream.readAllBytes())
                        .statusCode(200)
                        .statusMessage("OK").build());
            } finally {
                Files.deleteIfExists(tempFile);
            }

            clientSocket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
