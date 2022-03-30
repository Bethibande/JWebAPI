package de.bethibande.web.tcp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.google.gson.Gson;

import de.bethibande.web.handlers.HandlerInstance;
import de.bethibande.web.handlers.MethodHandle;
import de.bethibande.web.response.ServerResponse;
import de.bethibande.web.response.StreamResponse;
import de.bethibande.web.utils.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ServerHandler implements HttpHandler {

    private final TCPServer server;

    public ServerHandler(TCPServer server) {
        this.server = server;
    }

    public TCPServer getServer() {
        return server;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI u = exchange.getRequestURI();
        String uri = u.getPath();

        for(HandlerInstance handlerInstance : this.server.getHandlerManager().getHandlers().values()) {
            String s = handlerInstance.getUri();
            if(!s.startsWith("/")) s = "/" + s;
            if(uri.startsWith(s)) {
                HandlerInstance h = this.server.getHandlerManager().getHandlers().get(handlerInstance.getUri());
                for(String s2 : h.getMethods().keySet()) {
                    if(uri.matches(("^" + s + "/" + s2 + "[/]?").replaceAll("//", "/"))) {
                        MethodHandle mh = h.getMethods().get(s2);
                        Headers headers = exchange.getRequestHeaders();
                        String method = exchange.getRequestMethod();

                        HashMap<String, String> query = new HashMap<>();
                        if(u.getQuery() != null) {
                            String[] qFields = u.getQuery().split("&");
                            for (String qField : qFields) {
                                String field = qField.split("=")[0];
                                String value = qField.contains("=") ? qField.split("=")[1] : null;
                                query.put(field.toLowerCase(), value);
                            }
                        }

                        Object obj = null;
                        try {
                            obj = this.server.getProcessor().processRequest(exchange.getRemoteAddress(), uri, method, exchange.getRequestBody(), h.getInstance(), mh, query, headers);
                        } catch(Throwable e) {
                            e.printStackTrace();
                        }


                        if(obj instanceof ServerResponse) {
                            ServerResponse r = (ServerResponse) obj;
                            for(String k : r.getHeaders().keySet()) {
                                String v = r.getHeaders().get(k);
                                exchange.getResponseHeaders().add(k, v);
                            }
                            exchange.sendResponseHeaders(r.getStatusCode(), 0);
                            exchange.close();
                            return;
                        }

                        if(obj instanceof StreamResponse) {
                            InputStream in = ((StreamResponse) obj).getStream();
                            long length = ((StreamResponse) obj).getLength();

                            exchange.sendResponseHeaders(200, length);

                            long readTotal = 0;
                            int read;
                            byte[] buffer = new byte[this.server.getBufferSize()];
                            while((read = in.read(buffer)) > 0) {
                                exchange.getResponseHeaders().add("Content-Type", "*");
                                exchange.getResponseBody().write(ArrayUtils.trim(buffer, 0, read));

                                readTotal += read;
                                if(readTotal >= length) break;
                            }

                            exchange.close();
                            return;
                        }

                        String json = new Gson().toJson(obj);
                        exchange.getResponseHeaders().add("Content-Type", "text/json");
                        exchange.sendResponseHeaders(200, json.getBytes(StandardCharsets.UTF_8).length);
                        exchange.getResponseBody().write(json.getBytes(StandardCharsets.UTF_8));
                        exchange.close();
                        return;
                    }
                }
            }
        }

        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }
}
