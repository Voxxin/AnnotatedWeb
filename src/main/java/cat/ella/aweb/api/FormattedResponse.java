package cat.ella.aweb.api;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormattedResponse {

    private static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";
    private static final int DEFAULT_STATUS_CODE = 200;

    private String httpVersion = DEFAULT_HTTP_VERSION;
    private int statusCode = DEFAULT_STATUS_CODE;
    private String statusMessage = "OK";
    private String contentType;
    private byte[] contentBytes;
    private Map<String, String> customHeaders = new HashMap<>();

    /**
     * Set the HTTP version for the response.
     *
     * @param httpVersion The HTTP version.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse httpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    /**
     * Set the status code for the response.
     *
     * @param statusCode The status code.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Set the status message for the response.
     *
     * @param statusMessage The status message.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse statusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * Set the content type for the response.
     *
     * @param contentType The content type.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the content for the response.
     *
     * @param content The content.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse content(String content) {
        if (content != null && !content.isEmpty()) this.contentBytes = content.getBytes(StandardCharsets.UTF_8);
        else this.contentBytes = new byte[0];
        return this;
    }

    /**
     * Set the content for the response.
     *
     * @param contentBytes The content bytes.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse content(byte[] contentBytes) {
        this.contentBytes = contentBytes;
        return this;
    }

    /**
     * Add a custom header to the response.
     *
     * @param name  The name of the header.
     * @param value The value of the header.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse addHeader(String name, String value) {
        customHeaders.put(name, value);
        return this;
    }

    /**
     * Build the formatted response.
     *
     * @return The formatted response as bytes.
     */
    public byte[] build() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(httpVersion).append(" ").append(statusCode).append(" ").append(statusMessage).append("\r\n")
                .append("Content-Type: ").append(contentType).append("\r\n");

        // Append custom headers
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            responseBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        if (contentBytes != null) {
            responseBuilder.append("Content-Length: ").append(contentBytes.length).append("\r\n\r\n");
            byte[] headerBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);

            byte[] responseBytes = new byte[headerBytes.length + contentBytes.length];
            System.arraycopy(headerBytes, 0, responseBytes, 0, headerBytes.length);
            System.arraycopy(contentBytes, 0, responseBytes, headerBytes.length, contentBytes.length);

            return responseBytes;
        } else {
            return responseBuilder.append("\r\n").toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}

