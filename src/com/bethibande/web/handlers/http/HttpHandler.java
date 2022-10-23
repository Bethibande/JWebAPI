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
import com.bethibande.web.util.ReflectUtils;
import com.sun.net.httpserver.HttpExchange;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private final JWebServer owner;

    public HttpHandler(JWebServer owner) {
        this.owner = owner;
    }

    public boolean matches(URI uri, WebRequest request) {
        if(uri.type().equals(URI.URIType.STRICT)) return uri.value().equals(request.getUri().getPath());
        if(uri.type().equals(URI.URIType.STRING)) return request.getUri().getPath().startsWith(uri.value());
        if(uri.type().equals(URI.URIType.REGEX)) return request.getUri().getPath().matches(uri.value());
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

                    request.setMethod(handler.getMethod());
                    RequestResponse response = handler.invoke(request);
                    request.setResponse(response);

                    while(request.getResponse().getContentData() != null && owner.getWriters().get(request.getResponse().getContentData().getClass()) == null) {
                        Class<? extends OutputHandler<?>> outputHandler = owner.getOutputHandler(request.getResponse().getContentData().getClass());
                        if(outputHandler == null) outputHandler = owner.getOutputHandler(Object.class);

                        OutputHandler<Object> out = (OutputHandler<Object>) ReflectUtils.createInstance(outputHandler);
                        out.update(request.getResponse().getContentData(), request);
                    }

                    response = request.getResponse();

                    exchange.getResponseHeaders().putAll(response.getHeader());
                    exchange.getResponseHeaders().set("Connection", "close");

                    exchange.sendResponseHeaders(response.getStatusCode(), response.getContentLength());

                    if(response.getContentLength() > 0) {
                        OutputWriter writer = owner.getWriter(response.getContentData().getClass());
                        if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");
                        writer.write(request, response);
                    }

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
