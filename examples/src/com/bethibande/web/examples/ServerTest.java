package com.bethibande.web.examples;

import com.bethibande.web.JWebServer;
import com.bethibande.web.examples.annotations.SecuredAnnotationHandler;
import com.bethibande.web.tcp.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class ServerTest {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();
        server.bind(new InetSocketAddress("0.0.0.0", 5544), 100);

        JWebServer jWebServer = new JWebServer()
                .withLogLevel(Level.ALL)
                .withMethodInvocationHandler(new SecuredAnnotationHandler())
                .autoLoad(ServerTest.class)
                .withContextFactory(SecuredContext::new);

        jWebServer.start(server);
    }

}
