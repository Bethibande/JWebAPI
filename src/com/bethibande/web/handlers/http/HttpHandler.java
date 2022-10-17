package com.bethibande.web.handlers.http;

import com.bethibande.web.JWebServer;
import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.io.OutputWriter;
import com.bethibande.web.performance.TimingGenerator;
import com.bethibande.web.response.RequestResponse;
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
            WebRequest request = new WebRequest(owner, exchange);

            for(URI uri : owner.getMethods().keySet()) {
                if(matches(uri, request)) {
                    timings.keyframe();

                    MethodHandler handler = owner.getMethods().get(uri);

                    request.setMethod(handler.getMethod());
                    RequestResponse response = handler.invoke(request);

                    timings.keyframe();

                    if(response.getContentData() == null) {
                        for(String key : response.getHeader().keySet()) {
                            exchange.getResponseHeaders().add(key, response.getHeader().get(key));
                        }

                        exchange.sendResponseHeaders(response.getStatusCode(), 0);
                        exchange.close();
                        return;
                    }

                    owner.handleOutput(response, request);
                    response = request.getResponse();

                    timings.keyframe();

                    for(String key : response.getHeader().keySet()) {
                        exchange.getResponseHeaders().add(key, response.getHeader().get(key));
                    }

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
    }
}
