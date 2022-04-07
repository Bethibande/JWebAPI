package de.bethibande.web.struct;

import com.sun.net.httpserver.Headers;
import de.bethibande.web.handlers.MethodHandle;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class ServerRequest {

    private final String uri;
    private final InetSocketAddress client;
    private final Headers headers;
    private final HashMap<String, String> query;
    private final String method;
    private final MethodHandle handle;

    private boolean accessStatus = true;

    public ServerRequest(String uri, InetSocketAddress client, Headers headers, HashMap<String, String> query, String method, MethodHandle handle) {
        this.uri = uri;
        this.client = client;
        this.headers = headers;
        this.query = query;
        this.method = method;
        this.handle = handle;
    }

    public boolean canAccess(IAccessManager manager) {
        accessStatus = manager.canAccess(client, uri, method, headers, handle, query);
        return accessStatus;
    }

    public boolean getAccessStatus() {
        return accessStatus;
    }

    public String getUri() {
        return uri;
    }

    public InetSocketAddress getClient() {
        return client;
    }

    public Headers getHeaders() {
        return headers;
    }

    public HashMap<String, String> getQuery() {
        return query;
    }

    public String getMethod() {
        return method;
    }

    public MethodHandle getHandle() {
        return handle;
    }
}
