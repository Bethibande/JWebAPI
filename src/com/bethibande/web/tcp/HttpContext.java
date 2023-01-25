package com.bethibande.web.tcp;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.util.List;
import java.util.Map;

public class HttpContext extends com.sun.net.httpserver.HttpContext {

    private final HttpServer owner;

    public HttpContext(final HttpServer owner) {
        this.owner = owner;
    }

    @Override
    public HttpHandler getHandler() {
        return null;
    }

    @Override
    public void setHandler(final HttpHandler handler) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public HttpServer getServer() {
        return owner;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public List<Filter> getFilters() {
        return null;
    }

    @Override
    public Authenticator setAuthenticator(final Authenticator auth) {
        return null;
    }

    @Override
    public Authenticator getAuthenticator() {
        return null;
    }
}
