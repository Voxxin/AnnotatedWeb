package cat.ella.aweb;

import cat.ella.aweb.annotations.Capture;
import cat.ella.aweb.annotations.Route;
import cat.ella.aweb.annotations.WebService;
import cat.ella.aweb.api.FormattedResponse;
import cat.ella.aweb.api.PublicFile;
import cat.ella.aweb.api.FormattedRequest;
import cat.ella.aweb.element.FileReader;
import org.junit.jupiter.api.Test;

public class webtest {

    @WebService(port = 8080)
    @Route(path = "/", errorRoute = true)
    public static byte[] errorRoute(FormattedRequest request) {
        return new FormattedResponse()
                .contentType("text/html")
                .content(FileReader.read(webtest.class, "assets/public/four0four.html"))
                .statusMessage("Page Not Found")
                .statusCode(404).httpVersion("HTTP/1.1").build();
    }

    @WebService(port = 8080)
    @Route(path = "/error")
    @Capture
    public static void capture(FormattedRequest request) {
        if (request.getBody() != null) System.out.println("Hooray");
    }


    @Test
    void runWebServer() {
        WebServer webServer = new WebServer(8080);
        webServer.add(new PublicFile(WebServer.class, "assets/public/", "/", 0, 0));
        webServer.start();
    }
}
