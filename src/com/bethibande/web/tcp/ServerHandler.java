package com.bethibande.web.tcp;

import com.bethibande.web.handlers.MethodHandle;
import com.bethibande.web.response.StreamResponse;
import com.bethibande.web.utils.ArrayUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.google.gson.Gson;

import com.bethibande.web.handlers.HandlerInstance;
import com.bethibande.web.response.ServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
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

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
            exchange.getResponseHeaders().add("Allow", "OPTIONS, POST, GET");
            exchange.getResponseHeaders().add("Accept", "*");
            exchange.sendResponseHeaders(200, -1);
            return;
        }

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

                        if(obj == null) {
                            exchange.sendResponseHeaders(200, -1);
                            exchange.close();
                            continue;
                        }

                        if(obj instanceof ServerResponse) {
                            ServerResponse r = (ServerResponse) obj;
                            if(r.getHeaders() != null) {
                                for (String k : r.getHeaders().keySet()) {
                                    String v = r.getHeaders().get(k);
                                    exchange.getResponseHeaders().add(k, v);
                                }
                            }
                            //byte[] msg = ("{\"id\":" + r.getStatusCode() + "}").getBytes(this.server.getCharset());
                            exchange.sendResponseHeaders(r.getStatusCode(), -1);
                            exchange.close();
                            return;
                        }

                        if(obj instanceof StreamResponse) {
                            StreamResponse str = (StreamResponse)obj;
                            InputStream in = str.getStream();
                            long length = str.getLength();
                            length = length == 0 ? -1: length;

                            exchange.getResponseHeaders().add("Content-Type", str.getContentType());
                            exchange.sendResponseHeaders(200, length);

                            long readTotal = 0;
                            int read;
                            byte[] buffer = new byte[this.server.getBufferSize()];
                            while((read = in.read(buffer)) > 0) {
                                exchange.getResponseBody().write(ArrayUtils.trim(buffer, 0, read));

                                readTotal += read;
                                if(readTotal >= length) break;
                            }

                            exchange.close();
                            return;
                        }

                        String json = new Gson().toJson(obj);
                        int length = json.getBytes(this.server.getCharset()).length;
                        exchange.getResponseHeaders().add("Content-Type", "text/json");
                        exchange.sendResponseHeaders(200, length == 0 ? -1: length);
                        exchange.getResponseBody().write(json.getBytes(this.server.getCharset()));
                        exchange.close();
                        return;
                    }
                }
            }
        }

        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }
}
