package com.bethibande.web.sessions;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.MetaData;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Please note that these Sessions are only temporary and not saved inside any database, these sessions will by default be deleted after 10 min of inactivity
 */
public class Session {

    private final UUID sessionId;
    private final JWebServer server;
    private final InetAddress owner;
    private final MetaData meta = new MetaData();

    public Session(UUID sessionId, JWebServer server, InetAddress owner) {
        this.sessionId = sessionId;
        this.server = server;
        this.owner = owner;
        meta.set("creationDate", System.currentTimeMillis());
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public JWebServer getServer() {
        return server;
    }

    public InetAddress getOwner() {
        return owner;
    }

    public MetaData getMeta() {
        return meta;
    }

    public Long getCreationDate() {
        return meta.getLong("creationDate");
    }

}
