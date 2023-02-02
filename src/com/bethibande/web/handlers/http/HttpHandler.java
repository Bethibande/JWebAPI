package com.bethibande.web.handlers.http;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.timings.TimingGenerator;
import com.bethibande.web.types.RequestWriter;
import com.bethibande.web.types.ServerInterface;
import com.bethibande.web.types.URIObject;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * An internal class handling all incoming requests.
 * This invokes your methods, runs ParameterProcessors and fires invocation handlers
 */
public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

    private final JWebServer owner;

    public HttpHandler(JWebServer owner) {
        this.owner = owner;
    }

    @Override
    public void handle(HttpExchange exchange) {
        TimingGenerator timingGenerator = new TimingGenerator();
        timingGenerator.start(7);
        try {
            final WebRequest request = new WebRequest(owner, exchange);
            final ServerInterface _interface = owner.getInterfaceByAddress(exchange.getHttpContext().getServer().getAddress());

            Session session = owner.getSession(exchange.getRemoteAddress().getAddress());
            if(session == null) session = owner.generateSession(exchange.getRemoteAddress().getAddress());

            final ServerContext context = owner.getContextFactory().createContext(
                    owner,
                    _interface,
                    session,
                    request
            );

            LocalServerContext.setContext(context);

            timingGenerator.keyframe(); // load session and context keyframe

            for(URIObject uri : owner.getMethods()) {
                if(!uri.isApplicable(request.getUri().getPath())) continue;
                timingGenerator.keyframe(); // find uri keyframe

                MethodHandler handler = owner.getMethods().get(uri);

                request.setMethod(handler.getMethod());
                RequestResponse response = handler.invoke(context);
                request.setResponse(response);

                timingGenerator.keyframe(); // invoke method keyframe

                if(response.getContentData() instanceof RequestResponse res) {
                    response = res;
                }

                final RequestWriter writer = response.getContentData() == null ? null: owner.createWriter(response.getContentData());
                if(writer != null) {
                    response.setContentLength(writer.getLength());
                }

                response = request.getResponse();

                timingGenerator.keyframe(); // handle return value keyframe

                Headers responseHeader = exchange.getResponseHeaders();
                responseHeader.putAll(response.getHeader());
                responseHeader.set("Connection", "close");
                responseHeader.set("Access-Control-Allow-Origin", "*");

                exchange.sendResponseHeaders(response.getStatusCode(), response.getContentLength());

                timingGenerator.keyframe(); // send header keyframe

                if(response.getContentLength() > 0) {
                    if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");

                    final OutputStream out = exchange.getResponseBody();
                    final int bufferSize = owner.getBufferSize();

                    while(writer.hasNext()) {
                        writer.write(out, bufferSize);
                        out.flush();
                    }
                    writer.reset();
                }

                timingGenerator.keyframe(); // writing keyframe

                exchange.close();
                break;
            }
        } catch(Throwable th) {
            owner.getErrorHandler().accept(th, LocalServerContext.getContext());
            handleError();
        }

        LocalServerContext.clearContext();
        timingGenerator.keyframe(); // clean up keyframe

        if(timingGenerator.isComplete()) {
            timingGenerator.convert(TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS);
            owner.getLogger().fine(String.format("Incoming %s > %s > %d microseconds", exchange.getRemoteAddress().toString().substring(1), exchange.getRequestURI().getPath(), timingGenerator.getTotalTime()));
            logTimings(owner.getLogger(), timingGenerator);
        }
    }

    private void handleError() {
        final ServerContext context = LocalServerContext.getContext();
        if(context == null) return;
        if(context.request().getExchange().getResponseCode() != -1) return; // checks if response has already been sent

        RequestResponse response = context.request().getResponse();

        if(response.getContentData() instanceof RequestResponse res) {
            response = res;
        }

        final RequestWriter writer = response.getContentData() == null ? null: owner.createWriter(response.getContentData());
        if(writer != null) {
            response.setContentLength(writer.getLength());
        }

        final HttpExchange exchange = context.request().getExchange();
        final Headers responseHeader = exchange.getResponseHeaders();
        responseHeader.putAll(response.getHeader());
        responseHeader.set("Connection", "close");
        responseHeader.set("Access-Control-Allow-Origin", "*");

        try {
            exchange.sendResponseHeaders(response.getStatusCode(), response.getContentLength());

            if(response.getContentLength() > 0) {
                if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");

                final OutputStream out = exchange.getResponseBody();
                final int bufferSize = owner.getBufferSize();

                while(writer.hasNext()) {
                    writer.write(out, bufferSize);
                    out.flush();
                }
                writer.reset();
            }

            exchange.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logTimings(Logger logger, TimingGenerator timings) {
        logger.finer(String.format("Timings > Total %d microseconds > Load Session and Context %d, find uri %d, invoke method %d, handle return value %d, send header %d, writing %d, clean up %d",
                                   timings.getTotalTime(),
                                   timings.getTiming(0),
                                   timings.getTiming(1),
                                   timings.getTiming(2),
                                   timings.getTiming(3),
                                   timings.getTiming(4),
                                   timings.getTiming(5),
                                   timings.getTiming(6)));
    }

}
