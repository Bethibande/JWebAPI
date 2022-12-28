package com.bethibande.web.examples;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.annotations.SecuredAnnotationProcessor;
import com.bethibande.web.examples.beans.ServiceBean;
import com.bethibande.web.examples.permission.SecuredHandler;
import com.bethibande.web.response.RequestResponse;

import java.util.logging.Level;

public class Main {

    public static void main(String[] args) {
        JWebServer server = new JWebServer()
                .withLogLevel(Level.FINE)
                .withLogLevel(Level.ALL) /* Uncomment this line to see full debug info */
                .withBindAddress(5544)
                .withMethodInvocationHandler(new SecuredAnnotationProcessor())
                .withHandler(TestHandler.class)
                .withHandler(SecuredHandler.class)
                .withErrorHandler(Main::handleError)
                .withContextFactory(SecuredContext::new);

        server.storeGlobalBean(new ServiceBean());
        server.start();
    }

    public static void handleError(final Throwable th, final ServerContext context) {
        context.server().getLogger().severe("Encountered an error: " + th.getLocalizedMessage() + " for path: " + context.request().getUri().getPath());
        context.request().setResponse(new RequestResponse()
                .withStatusCode(500)
                .withContentData(new Message(500, "Internal server error")));
    }

}
