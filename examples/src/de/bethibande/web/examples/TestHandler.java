package de.bethibande.web.examples;

import de.bethibande.web.annotations.*;
import de.bethibande.web.handlers.WebHandler;
import de.bethibande.web.response.ServerResponse;
import de.bethibande.web.struct.ServerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * This will automatically turn the received post data into method parameters
     * {"username":"usernameValue","password":"passwordValue"}
     * the json fields will automatically be filled into the java parameters with the same name, if you are not using the javac -parameters option
     * you need to annotate the json field name using @FieldName("...")
     */
    @URI("/login")
    @JsonMappings
    public static boolean login(@FieldName("username") String username, @FieldName("password") String password) {
        System.out.println("Login: " + username + "; " + password);

        return ThreadLocalRandom.current().nextBoolean();
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
            long len = in == null ? 0: in.available();

            return ServerResponse.stream(in, "text/html", len);
        } catch(IOException e) {
            e.printStackTrace();
            return ServerResponse.httpStatusCode(500);
        }
    }

    @URI("/error")
    public static Object error() {
        return ServerResponse.httpStatusCode(400);
    }

    @URI("/status/{statusCode:num}")
    public Object upload(@URIField("statusCode") int id) {

        System.out.println("error id: " + id);

        return ServerResponse.httpStatusCode(id);
    }

    @URI("/test2/{name:string(3,16)}")
    public static Object test2(@URIField("name") String name) {
        return new Message(1, "Hello " + name + "!");
    }

    @URI("/localOnly") // 127.0.0.1 address only
    public static Object localOnly(ServerRequest request) {
        if(!request.canAccess(Accessors::localOnly)) return null;

        return new Message(1, "Access Granted!");
    }

}
