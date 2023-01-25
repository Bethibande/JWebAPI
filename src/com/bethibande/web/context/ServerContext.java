package com.bethibande.web.context;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.ServerInterface;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.sessions.Session;

public class ServerContext {

    private final JWebServer server;
    private final ServerInterface serverInterface;
    private final Session session;
    private final WebRequest request;

    private final ContextMeta metaData = new ContextMeta();

    public ServerContext(final JWebServer server, final ServerInterface serverInterface, final Session session, final WebRequest request) {
        this.server = server;
        this.serverInterface = serverInterface;
        this.session = session;
        this.request = request;

        this.metaData.setBufferSize(server.getBufferSize());
        this.metaData.setCharset(server.getCharset());
    }

    public JWebServer server() {
        return server;
    }

    public ServerInterface serverInterface() {
        return serverInterface;
    }

    public Session session() {
        return session;
    }

    public WebRequest request() {
        return request;
    }

    public ContextMeta metadata() {
        return metaData;
    }
}
