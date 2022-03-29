package de.bethibande.web.examples;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import de.bethibande.web.JWebClient;
import de.bethibande.web.JWebServer;
import de.bethibande.web.response.StreamResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {
        JWebServer server = JWebServer.tcp(5566);
        server.registerHandler(TestHandler.class);
        server.start();

        JWebClient<Client> client = JWebClient.of(Client.class, "http://127.0.0.1:5566");
        Client db = client.getInstance();

        printAsJson(db.test());
        printAsJson(db.getMessage("Max"));

        StreamResponse sr = db.getFile();
        byte[] buffer = sr.getStream().readAllBytes();
        System.out.println("File: " + new String(buffer, StandardCharsets.UTF_8));

        server.stop();
    }

    public static void printAsJson(Object obj) {
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
    }

}
