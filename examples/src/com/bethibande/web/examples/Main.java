package com.bethibande.web.examples;

import com.bethibande.web.JWebServer;
import com.bethibande.web.annotations.URI;

public class Main {

    public static void main(String[] args) {
        JWebServer server = new JWebServer()
                .withBindAddress(5544)
                .withHandler(TestHandler.class);

        server.start();

        System.out.println("Running!");
    }

}
