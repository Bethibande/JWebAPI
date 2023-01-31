package com.bethibande.web.types;

import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class Request {

    /**
     * Create Request Object from parameters
     * @throws RuntimeException wrapping {@link MalformedURLException}
     */
    public static Request ofString(final String url, final RequestMethod method) {
        return new Request(URI.create(url), new Headers(), method, null);
    }

    private URI uri;
    private Headers headers;
    private RequestMethod method;
    private @Nullable RequestWriter writer;
    private Object responseData;

    public Request(final URI uri,
                   final Headers headers,
                   final RequestMethod method,
                   final @Nullable RequestWriter writer) {
        this.uri = uri;
        this.headers = headers;
        this.method = method;
        this.writer = writer;
    }

    public Request(final URI uri,
                   final Headers headers,
                   final RequestMethod method,
                   final @Nullable RequestWriter writer,
                   final Object responseData) {
        this.uri = uri;
        this.headers = headers;
        this.method = method;
        this.writer = writer;
        this.responseData = responseData;
    }

    public Request withUri(final URI uri) {
        setUri(uri);
        return this;
    }

    public Request withHeaders(final Headers headers) {
        setHeaders(headers);
        return this;
    }

    public Request withMethod(final RequestMethod method) {
        setMethod(method);
        return this;
    }

    public Request withWriter(final @Nullable RequestWriter writer) {
        setWriter(writer);
        return this;
    }

    public Request withResponseData(final Object responseData) {
        setResponseData(responseData);
        return this;
    }

    public void setResponseData(final Object responseData) {
        this.responseData = responseData;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    public void setHeaders(final Headers headers) {
        this.headers = headers;
    }

    public void setMethod(final RequestMethod method) {
        this.method = method;
    }

    public void setWriter(final @Nullable RequestWriter writer) {
        this.writer = writer;
    }

    public URI uri() {
        return uri;
    }

    public Headers headers() {
        return headers;
    }

    public RequestMethod method() {
        return method;
    }

    public RequestWriter writer() {
        return writer;
    }

    public Object responseData() {
        return responseData;
    }

}
