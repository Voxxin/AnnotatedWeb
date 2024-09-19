package cat.ella.aweb.api;

import java.util.HashMap;
import java.util.List;

public class FormattedRequest {

    private HashMap<String, String> headers;
    private String body;
    private String method;
    private String path;
    private String pathParameters;
    private HashMap<String, String> query;
    private String httpVersion;

    /**
     * Constructor for FormattedRequest.
     *
     * @param inputHeaders The list of input headers.
     */
    public FormattedRequest(List<String> inputHeaders) {
        boolean hasBody;
        if (inputHeaders.get(0).contains("HTTP/")) hasBody = false;
        else if (inputHeaders.get(1).contains("HTTP/")) hasBody = true;
        else return;

        if (hasBody) { this.body = inputHeaders.get(0); inputHeaders.remove(0); }

        String[] mainMethods = inputHeaders.remove(0).split(" ");
        this.method = mainMethods[0];
        String fullPath = mainMethods[1];
        this.httpVersion = mainMethods[2];

        this.headers = new HashMap<>();
        this.query = new HashMap<>();

        int queryIndex = fullPath.indexOf('?');
        if (queryIndex != -1) {
            String[] pathAndQuery = fullPath.split("\\?", 2);
            this.path = pathAndQuery[0];
            String[] queryParams = pathAndQuery[1].split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=", 2);
                query.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
            }
        } else {
            this.path = fullPath;
        }

        int pathParamIndex = path.indexOf(':');
        if (pathParamIndex != -1) {
            this.pathParameters = path.substring(pathParamIndex + 1);
            this.path = path.substring(0, pathParamIndex);
        } else {
            this.pathParameters = null;
        }

        for (String header : inputHeaders) {
            String[] parts = header.split(": ", 2);
            if (parts.length == 2) {
                headers.put(parts[0], parts[1]);
            }
        }
    }

    /**
     * Get the request body.
     *
     * @return The request body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get the request method.
     *
     * @return The request method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the request path.
     *
     * @return The request path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the path parameters.
     *
     * @return The path parameters.
     */
    public String getPathParameters() {
        return pathParameters;
    }

    /**
     * Get the request query parameters.
     *
     * @return The request query parameters.
     */
    public HashMap<String, String> getQuery() {
        return query;
    }

    /**
     * Get the HTTP version.
     *
     * @return The HTTP version.
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Get the request headers.
     *
     * @return A HashMap representing the request headers, where keys are header names and values are header values.
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
