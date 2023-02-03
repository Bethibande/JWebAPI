package com.bethibande.web.examples.chat.context;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.examples.chat.ChatServer;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.MetaData;
import com.bethibande.web.types.ServerInterface;
import com.bethibande.web.types.WebRequest;

import java.util.ArrayList;
import java.util.List;

public class ChatContext extends ServerContext {

    public static final String META_NAME = "chat.name";

    public ChatContext(final JWebServer server,
                       final ServerInterface serverInterface,
                       final Session session,
                       final WebRequest request) {
        super(server, serverInterface, session, request);
    }

    @SuppressWarnings("unchecked")
    public boolean hasAuth() {
        final MetaData meta = session().getMeta();

        if(!meta.hasMeta(META_NAME)) return false;
        return !((List<String>) meta.get(META_NAME)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void addName(final String name) {
        final MetaData meta = session().getMeta();

        if(!meta.hasMeta(META_NAME)) {
            meta.set(META_NAME, new ArrayList<String>());
        }
        ((List<String>) meta.get(META_NAME)).add(name);
    }

    @SuppressWarnings("unchecked")
    public boolean ownsName(final String name) {
        final MetaData meta = session().getMeta();

        if(!meta.hasMeta(META_NAME)) return false;
        return ((List<String>) meta.get(META_NAME)).contains(name);
    }

    public void removeName(final String name) {
        final MetaData meta = session().getMeta();

        if(!meta.hasMeta(META_NAME)) return;
        ((List<String>) meta.get(META_NAME)).remove(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void finalize() throws Throwable {
        final MetaData meta = session().getMeta();

        if(!meta.hasMeta(META_NAME)) return;

        final List<String> names = ((List<String>) meta.get(META_NAME));

        for(String name : names) {
            if(!ChatServer.names.contains(name.toLowerCase())) continue;
            ChatServer.names.remove(name.toLowerCase());
            ChatServer.createMessage("System", "User disconnected: " + name);
        }
    }
}
