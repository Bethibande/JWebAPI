package com.bethibande.web.types;

import com.bethibande.web.JWebServer;
import com.bethibande.web.response.RequestResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;

public class WebRequest {

    private URI uri;
    private Headers requestHeaders;

    private JWebServer server;
    private HttpExchange exchange;

    private RequestResponse response;

    private boolean isFinished = false;

    private Method method;
    private Object[] methodInvocationParameters;

    private QueryMap queryMap;

    public WebRequest(JWebServer server, HttpExchange exchange) {
        this.server = server;
        this.exchange = exchange;

        this.response = new RequestResponse();
        this.response.setCharset(server.getCharset());
        this.uri = exchange.getRequestURI();
        this.requestHeaders = exchange.getRequestHeaders();
    }

    private void parseQuery() {
        queryMap = new QueryMap(getUri().getQuery());
    }

    public QueryMap getQuery() {
        if(queryMap == null) parseQuery();
        return queryMap;
    }

    public JWebServer getServer() {
        return server;
    }

    public void setParameter(int i, Object value) {
        methodInvocationParameters[i] = value;
    }

    public void setResponse(RequestResponse response) {
        this.response = response;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public void setMethod(Method method) {
        this.method = method;
        this.methodInvocationParameters = new Object[method.getParameterCount()];
    }

    public void setMethodInvocationParameters(Object[] methodInvocationParameters) {
        this.methodInvocationParameters = methodInvocationParameters;
    }

    public URI getUri() {
        return uri;
    }

    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public long getContentLength() {
        String value = getRequestHeaders().getFirst("Content-Length");
        if(value == null) return 0L;
        return Long.parseLong(value);
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    public RequestResponse getResponse() {
        return response;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getMethodInvocationParameters() {
        return methodInvocationParameters;
    }

    @Override
    public WebRequest clone() {
        return new WebRequest(server, exchange);
    }
}
