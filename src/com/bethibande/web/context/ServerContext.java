package com.bethibande.web.context;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.sessions.Session;
import com.sun.net.httpserver.HttpExchange;

public class ServerContext {

    private final JWebServer server;
    private final Session session;
    private final HttpExchange exchange;
    private final WebRequest request;

    public ServerContext(JWebServer server, Session session, HttpExchange exchange, WebRequest request) {
        this.server = server;
        this.session = session;
        this.exchange = exchange;
        this.request = request;
    }

    public JWebServer server() {
        return server;
    }

    public Session session() {
        return session;
    }

    public HttpExchange exchange() {
        return exchange;
    }

    public WebRequest request() {
        return request;
    }

}
