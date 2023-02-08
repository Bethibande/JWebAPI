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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpExchange extends com.sun.net.httpserver.HttpExchange {

    public static final HashMap<Integer, String> STATUS_MESSAGES = new HashMap<>();

    static {
        STATUS_MESSAGES.put(100, "CONTINUE");
        STATUS_MESSAGES.put(101, "SWITCHING PROTOCOLS");
        STATUS_MESSAGES.put(103, "EARLY HINTS");
        STATUS_MESSAGES.put(200, "OK");
        STATUS_MESSAGES.put(201, "CREATED");
        STATUS_MESSAGES.put(202, "ACCEPTED");
        STATUS_MESSAGES.put(203, "NON-AUTHORITATIVE INFORMATION");
        STATUS_MESSAGES.put(204, "NO CONTENT");
        STATUS_MESSAGES.put(205, "RESET CONTENT");
        STATUS_MESSAGES.put(206, "PARTIAL CONTENT");
        STATUS_MESSAGES.put(300, "MULTIPLE CHOICES");
        STATUS_MESSAGES.put(301, "MOVED PERMANENTLY");
        STATUS_MESSAGES.put(302, "FOUND");
        STATUS_MESSAGES.put(303, "SEE OTHER");
        STATUS_MESSAGES.put(304, "NOT MODIFIED");
        STATUS_MESSAGES.put(305, "USE PROXY");
        STATUS_MESSAGES.put(306, "UNUSED");
        STATUS_MESSAGES.put(307, "TEMPORARY REDIRECT");
        STATUS_MESSAGES.put(308, "PERMANENT REDIRECT");
        STATUS_MESSAGES.put(400, "BAD REQUEST");
        STATUS_MESSAGES.put(401, "UNAUTHORIZED");
        STATUS_MESSAGES.put(402, "PAYMENT REQUIRED");
        STATUS_MESSAGES.put(403, "FORBIDDEN");
        STATUS_MESSAGES.put(404, "NOT FOUND");
        STATUS_MESSAGES.put(405, "METHOD NOT ALLOWED");
        STATUS_MESSAGES.put(406, "NOT ACCEPTABLE");
        STATUS_MESSAGES.put(407, "PROXY AUTHENTICATION REQUIRED");
        STATUS_MESSAGES.put(408, "REQUEST TIMEOUT");
        STATUS_MESSAGES.put(409, "CONFLICT");
        STATUS_MESSAGES.put(410, "GONE");
        STATUS_MESSAGES.put(411, "LENGTH REQUIRED");
        STATUS_MESSAGES.put(412, "PRECONDITION FAILED");
        STATUS_MESSAGES.put(413, "PAYLOAD TOO LARGE");
        STATUS_MESSAGES.put(414, "URI TOO LONG");
        STATUS_MESSAGES.put(415, "UNSUPPORTED MEDIA TYPE");
        STATUS_MESSAGES.put(416, "RANGE NOT SATISFIABLE");
        STATUS_MESSAGES.put(417, "EXPECTATION FAILED");
        STATUS_MESSAGES.put(418, "I'M A TEAPOT");
        STATUS_MESSAGES.put(422, "UNPROCESSABLE ENTITY");
        STATUS_MESSAGES.put(425, "TOO EARLY");
        STATUS_MESSAGES.put(426, "UPGRADE REQUIRED");
        STATUS_MESSAGES.put(428, "PRECONDITION REQUIRED");
        STATUS_MESSAGES.put(429, "TOO MANY REQUESTS");
        STATUS_MESSAGES.put(431, "REQUEST HEADER FIELDS TOO LARGE");
        STATUS_MESSAGES.put(451, "UNAVAILABLE FOR LEGAL REASONS");
        STATUS_MESSAGES.put(500, "INTERNAL ERROR");
        STATUS_MESSAGES.put(501, "NOT IMPLEMENTED");
        STATUS_MESSAGES.put(502, "BAD GATEWAY");
        STATUS_MESSAGES.put(503, "SERVICE UNAVAILABLE");
        STATUS_MESSAGES.put(504, "GATEWAY TIMEOUT");
        STATUS_MESSAGES.put(505, "HTTP VERSION NOT SUPPORTED");
        STATUS_MESSAGES.put(506, "VARIANT ALSO NEGOTIATES");
        STATUS_MESSAGES.put(507, "INSUFFICIENT STORAGE");
        STATUS_MESSAGES.put(508, "LOOP DETECTED");
        STATUS_MESSAGES.put(510, "NOT EXTENDED");
        STATUS_MESSAGES.put(511, "NETWORK AUTHENTICATION REQUIRED");
    }

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

        StringBuilder sb = new StringBuilder(version + " " + rCode + " " + STATUS_MESSAGES.get(rCode) + "\r\n");
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
