package com.bethibande.web;

import com.bethibande.web.response.RequestResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;
import java.net.URI;

public class WebRequest {

    private URI uri;
    private Headers requestHeaders;

    private JWebServer server;
    private HttpExchange exchange;

    private RequestResponse response;

    private boolean isFinished = false;

    private Method method;
    private Object[] methodInvocationParameters;

    public WebRequest(JWebServer server, HttpExchange exchange) {
        this.server = server;
        this.exchange = exchange;

        this.response = new RequestResponse();
        this.uri = exchange.getRequestURI();
        this.requestHeaders = exchange.getRequestHeaders();
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
}
