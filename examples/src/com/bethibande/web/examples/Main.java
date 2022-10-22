package com.bethibande.web.examples;

import com.bethibande.web.JWebServer;
import com.bethibande.web.examples.permission.SecuredHandler;

public class Main {

    public static void main(String[] args) {
        JWebServer server = new JWebServer()
                .withBindAddress("127.0.0.1", 5544)
                .withHandler(TestHandler.class)
                .withHandler(SecuredHandler.class)
                .withContextFactory(SecuredContext::new);

        server.start();

        System.out.println("Running!");
    }

}
