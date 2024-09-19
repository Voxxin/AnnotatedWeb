package cat.ella.aweb.annotations;

import java.lang.annotation.*;

/**
 * Specifies the port number for a web service.
 * This annotation is used to define the port on which the annotated web service should listen.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @WebService(port = 8080)
 * @Route(path = "/user")
 * @Capture(method = "PUT")
 * public void updateUserDetails(UserRequest request) {
 *     // Handle PUT request at /user to update user details
 * }
 * }
 * </pre>
 *
 * @see Route
 * @see Capture
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebService {
    int port();
}

