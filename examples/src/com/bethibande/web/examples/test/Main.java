package com.bethibande.web.examples.test;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.test.annotations.SecuredAnnotationHandler;
import com.bethibande.web.examples.test.beans.ServiceBean;
import com.bethibande.web.examples.test.permission.SecuredHandler;
import com.bethibande.web.response.RequestResponse;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) {
        JWebServer server = new JWebServer()
                .withLogLevel(Level.FINE)
                //.withLogLevel(Level.ALL) /* Uncomment this line to see full debug info */
                .withMethodInvocationHandler(new SecuredAnnotationHandler())
                .withHandler(TestHandler.class)
                .withHandler(SecuredHandler.class)
                .withErrorHandler(Main::handleError)
                .withContextFactory(SecuredContext::new);

        server.storeGlobalBean(new ServiceBean());
        server.start(new InetSocketAddress("127.0.0.1", 5544)); // binding to ipv4 loopback address
        server.start(new InetSocketAddress("::1", 5544)); // binding to ipv6 loopback address
    }

    public static void handleError(final Throwable th, final ServerContext context) {
        context.api().getLogger().severe("Encountered an error: " + th.getLocalizedMessage() + " for path: " + context.request().getUri().getPath());
        context.request().setResponse(new RequestResponse()
                .withStatusCode(500)
                .withContentData(new Message(500, "Internal server error")));
    }

}
