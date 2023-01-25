package com.bethibande.web.context;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.ServerInterface;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.sessions.Session;

@FunctionalInterface
public interface ContextFactory {

    ServerContext createContext(final JWebServer server,
                                final ServerInterface _interface,
                                final Session session,
                                final WebRequest request);

}
