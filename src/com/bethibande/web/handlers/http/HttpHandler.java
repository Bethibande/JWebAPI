package com.bethibande.web.handlers.http;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.handlers.out.OutputHandler;
import com.bethibande.web.io.OutputWriter;
import com.bethibande.web.performance.TimingGenerator;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.util.ReflectUtils;
import com.sun.net.httpserver.HttpExchange;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private final JWebServer owner;

    private final TimingGenerator timings = new TimingGenerator();

    public HttpHandler(JWebServer owner) {
        this.owner = owner;

        timings.setLabel("start", "find uri", "invoke", "handle output", "copy headers", "write data", "close");
    }

    public boolean matches(URI uri, WebRequest request) {
        if(uri.type().equals(URI.URIType.STRICT)) return uri.value().equals(request.getUri().getPath());
        if(uri.type().equals(URI.URIType.STRING)) return request.getUri().getPath().startsWith(uri.value());
        if(uri.type().equals(URI.URIType.REGEX)) return request.getUri().getPath().matches(uri.value());
        return false;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            timings.start();
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
                    timings.keyframe();

                    MethodHandler handler = owner.getMethods().get(uri);

                    request.setMethod(handler.getMethod());
                    RequestResponse response = handler.invoke(request);

                    timings.keyframe();

                    while(request.getResponse().getContentData() != null && owner.getWriters().get(request.getResponse().getContentData().getClass()) == null) {
                        Class<? extends OutputHandler<?>> outputHandler = owner.getOutputHandler(request.getResponse().getContentData().getClass());
                        if(outputHandler == null) outputHandler = owner.getOutputHandler(Object.class);

                        OutputHandler<Object> out = (OutputHandler<Object>) ReflectUtils.createInstance(outputHandler);
                        out.update(request.getResponse().getContentData(), request);
                    }

                    response = request.getResponse();

                    timings.keyframe();

                    exchange.getResponseHeaders().putAll(response.getHeader());

                    exchange.sendResponseHeaders(response.getStatusCode(), response.getContentLength());

                    timings.keyframe();

                    if(response.getContentLength() > 0) {
                        OutputWriter writer = owner.getWriter(response.getContentData().getClass());
                        if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");
                        writer.write(request, response);
                    }

                    timings.keyframe();

                    exchange.close();

                    timings.keyframe();
                    timings.stop();
                    break;
                }
            }
        } catch(Throwable th) {
            th.printStackTrace();
        }

        LocalServerContext.clearContext();
    }
}
