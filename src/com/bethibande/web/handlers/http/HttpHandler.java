package com.bethibande.web.handlers.http;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.handlers.out.OutputHandler;
import com.bethibande.web.io.OutputWriter;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.HttpExchange;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private final JWebServer owner;

    public HttpHandler(JWebServer owner) {
        this.owner = owner;
    }

    public boolean matches(URI uri, WebRequest request) { // TODO: refactor to remove duplicate code
        URI.URIType type = uri.type();
        String path = request.getUri().getPath();
        String value = uri.value();
        if(type.equals(URI.URIType.STRICT)) return path.equalsIgnoreCase(value);
        if(type.equals(URI.URIType.STRING)) return path.startsWith(value);
        if(type.equals(URI.URIType.REGEX)) return path.matches(value);
        return false;
    }

    @Override
    public void handle(HttpExchange exchange) {
        long start = System.currentTimeMillis();
        try {
            final WebRequest request = new WebRequest(owner, exchange);

            Session session = owner.getSession(exchange.getRemoteAddress().getAddress());
            if(session == null) session = owner.generateSession(exchange.getRemoteAddress().getAddress());

            LocalServerContext.setContext(owner.getContextFactory().createContext(
                    owner,
                    session,
                    exchange,
                    request
            ));

            for(URI uri : owner.getMethods().keySet()) {
                if(matches(uri, request)) {
                    MethodHandler handler = owner.getMethods().get(uri);

                    //System.out.println((System.currentTimeMillis() - start) + " ms");

                    request.setMethod(handler.getMethod());
                    RequestResponse response = handler.invoke(request);
                    request.setResponse(response);
                    //System.out.println((System.currentTimeMillis() - start) + " ms");

                    request.getExchange().getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                    // TODO: !! this while block is slow, refactor this, store outputhandlers as object instances and not classes !!
                    while(request.getResponse().getContentData() != null && owner.getWriters().get(request.getResponse().getContentData().getClass()) == null) {
                        OutputHandler<?> outputHandler = owner.getOutputHandler(request.getResponse().getContentData().getClass());
                        if(outputHandler == null) outputHandler = owner.getOutputHandler(Object.class);

                        ((OutputHandler<Object>) outputHandler).update(request.getResponse().getContentData(), request);
                    }

                    response = request.getResponse();
                    //System.out.println((System.currentTimeMillis() - start) + " ms");

                    exchange.getResponseHeaders().putAll(response.getHeader());
                    exchange.getResponseHeaders().set("Connection", "close");

                    exchange.sendResponseHeaders(response.getStatusCode(), response.getContentLength());

                    if(response.getContentLength() > 0) {
                        OutputWriter writer = owner.getWriter(response.getContentData().getClass());
                        if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");
                        writer.write(request, response);
                    }

                    //System.out.println((System.currentTimeMillis() - start) + " ms");
                    exchange.close();
                    break;
                }
            }
        } catch(Throwable th) {
            if(owner.isDebug()) th.printStackTrace();
        }

        LocalServerContext.clearContext();
        long end = System.currentTimeMillis();
        if(owner.isDebug()) System.out.println((end-start) + " ms");
    }
}
