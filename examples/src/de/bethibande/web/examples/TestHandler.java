package de.bethibande.web.examples;

import de.bethibande.web.URI;
import de.bethibande.web.annotations.QueryField;
import de.bethibande.web.handlers.WebHandler;
import de.bethibande.web.response.ServerResponse;

import java.io.IOException;
import java.io.InputStream;

@URI("/api")
public class TestHandler implements WebHandler {

    public static class Message {

        private final int id;
        private final String message;

        public Message(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }
    }

    @URI("/test")
    public static Object test(@QueryField(value = "name", def = "World") String name, @QueryField(value = "id", def = "1") int id) {
        return new Message(id, "Hello " + name + "!");
    }

    @URI("/") // uri = /api/
    public static Object test2() {
        return ServerResponse.redirect("/api/test");
    }

    @URI("/file")
    public static Object file(@QueryField(value = "file", def = "test.html") String f) {
        if(!f.equals("test.html")) return ServerResponse.httpStatusCode(404);

        try {
            InputStream in = TestHandler.class.getResourceAsStream("/test.html");
            long len = in.available();

            return ServerResponse.stream(in, "text/html", len);
        } catch(IOException e) {
            e.printStackTrace();
            return ServerResponse.httpStatusCode(500);
        }
    }

}
