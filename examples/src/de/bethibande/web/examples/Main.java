package de.bethibande.web.examples;

import de.bethibande.web.JWebServer;

public class Main {

    public static void main(String[] args) {
        JWebServer server = JWebServer.tcp(5566);
        server.registerHandler(TestHandler.class);
        server.start();

    }

}
