package com.bethibande.web.handlers.http;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.timings.TimingGenerator;
import com.bethibande.web.types.URIObject;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.handlers.MethodHandler;
import com.bethibande.web.handlers.out.OutputHandler;
import com.bethibande.web.io.OutputWriter;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

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
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange exchange) {
        TimingGenerator timingGenerator = new TimingGenerator();
        timingGenerator.start(7);
        try {
            final WebRequest request = new WebRequest(owner, exchange);

            Session session = owner.getSession(exchange.getRemoteAddress().getAddress());
            if(session == null) session = owner.generateSession(exchange.getRemoteAddress().getAddress());

            final ServerContext context = owner.getContextFactory().createContext(
                    owner,
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


                while(request.getResponse().getContentData() != null && owner.getWriters().get(request.getResponse().getContentData().getClass()) == null) {
                    OutputHandler<?> outputHandler = owner.getOutputHandler(request.getResponse().getContentData().getClass());
                    if(outputHandler == null) outputHandler = owner.getOutputHandler(Object.class);

                    ((OutputHandler<Object>) outputHandler).update(request.getResponse().getContentData(), request);
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
                    OutputWriter writer = owner.getWriter(response.getContentData().getClass());
                    if(writer == null) throw new RuntimeException("There is no writer for the type: '" + response.getContentData().getClass() + "'!");
                    writer.write(request, response);
                }

                timingGenerator.keyframe(); // writing keyframe

                exchange.close();
                break;
            }
        } catch(Throwable th) {
            th.printStackTrace();
        }

        LocalServerContext.clearContext();
        timingGenerator.keyframe(); // clean up keyframe

        if(timingGenerator.isComplete()) {
            timingGenerator.convert(TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS);
            owner.getLogger().fine(String.format("Incoming %s > %s > %d microseconds", exchange.getRemoteAddress().toString().substring(1), exchange.getRequestURI().getPath(), timingGenerator.getTotalTime()));
            logTimings(owner.getLogger(), timingGenerator);
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
