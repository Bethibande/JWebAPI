package com.bethibande.web.context;

import com.bethibande.web.sessions.Session;

public class LocalServerContext {

    private static final ThreadLocal<ServerContext> context = ThreadLocal.withInitial(() -> null);

    public static void clearContext() {
        context.remove();
    }

    public static void setContext(ServerContext context) {
        LocalServerContext.context.set(context);
    }

    public static ServerContext getContext() {
        return context.get();
    }

    public static boolean hasContext() {
        return getContext() != null;
    }

    public static Session getSession() {
        if(hasContext()) return getContext().session();
        return null;
    }

}
