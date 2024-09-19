package cat.ella.aweb.annotations;

import java.lang.annotation.*;

/**
 * Specifies the HTTP method for a web service endpoint.
 * This annotation is used in conjunction with {@link WebService} and {@link Route}
 * to define the HTTP method (e.g., GET, POST) for the annotated method.
 *
 * <p>Defaults to "GET" if not specified.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @WebService(port = 2020)
 * @Route(path = "/")
 * @Capture(method = "POST")
 * public void handlePostRequest(FormattedRequest request) {
 *     if (request.getBody() != null) {
 *         System.out.println("Request received");
 *     }
 * }
 * }
 * </pre>
 *
 * @see WebService
 * @see Route
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Capture {
    String method() default "GET";
}



