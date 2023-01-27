package com.bethibande.web.tcp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpExchange extends com.sun.net.httpserver.HttpExchange {

    private final Socket owner;
    private final com.bethibande.web.tcp.HttpContext context;

    private final String method;
    private final URI uri;
    private final String version;
    private final Headers requestHeaders;

    private final Headers responseHeaders = new Headers();

    private int responseCode = -1;

    public HttpExchange(final Socket owner,
                        final HttpServer server,
                        final String method,
                        final URI uri,
                        final String version,
                        final Headers requestHeaders) {
        this.owner = owner;
        this.context = new com.bethibande.web.tcp.HttpContext(server);
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.requestHeaders = requestHeaders;
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public HttpContext getHttpContext() {
        return context;
    }

    @Override
    public void close() {
        try {
            owner.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getRequestBody() {
        try {
            return owner.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getResponseBody() {
        try {
            return owner.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.responseCode = rCode;
        this.responseHeaders.set("Content-Length", String.valueOf(responseLength));

        StringBuilder sb = new StringBuilder(version + " " + rCode + " OK\r\n"); // TODO: Message
        for(Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            for(String value : entry.getValue()) {
                sb.append(entry.getKey()).append(": ").append(value).append("\r\n");
            }
        }
        sb.append("\r\n");

        OutputStream out = owner.getOutputStream();
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) owner.getRemoteSocketAddress();
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) owner.getLocalSocketAddress();
    }

    @Override
    public String getProtocol() {
        return version;
    }

    @Override
    public Object getAttribute(String name) {
        throw new RuntimeException("Action not Supported!");
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new RuntimeException("Action not Supported!");
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        throw new RuntimeException("Action not Supported!");
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return null;
    }
}
