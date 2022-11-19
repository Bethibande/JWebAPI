package com.bethibande.web.context;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.sessions.Session;

@FunctionalInterface
public interface ContextFactory {

    ServerContext createContext(JWebServer server, Session session, WebRequest request);

}
