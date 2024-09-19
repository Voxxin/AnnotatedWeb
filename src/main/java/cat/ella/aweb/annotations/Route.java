package cat.ella.aweb.annotations;

import java.lang.annotation.*;

/**
 * Specifies the URL path for a web service endpoint.
 * This annotation is used to define the route for the annotated method,
 * either in conjunction with {@link WebService} and {@link Capture}
 * or standalone for handling requests that return a byte array.
 *
 * <p>When used with {@link Capture}, it defines the URL path and HTTP method
 * for the endpoint.</p>
 *
 * <p>When used alone, the method should return a byte array.</p>
 *
 * <p>Example usage with {@link Capture}:</p>
 * <pre>
 * {@code
 * @WebService(port = 2020)
 * @Route(path = "/api/resource")
 * @Capture(method = "POST")
 * public void handlePostRequest(FormattedRequest request) {
 *     // Handle POST request at /api/resource
 * }
 * }
 * </pre>
 *
 * <p>Example usage without {@link Capture}:</p>
 * <pre>
 * {@code
 * @WebService(port = 2020)
 * @Route(path = "/api/download")
 * public byte[] handleDownloadRequest() {
 *     // Handle request at /api/download and return byte array
 *     return someByteArray;
 * }
 * }
 * </pre>
 *
 * @see WebService
 * @see Capture
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String path();
    boolean errorRoute() default false;
}

