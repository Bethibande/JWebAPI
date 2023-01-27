package com.bethibande.web.types;

import com.sun.net.httpserver.Headers;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

public class Request {

    /**
     * Create Request Object from parameters
     * @throws RuntimeException wrapping {@link MalformedURLException}
     */
    public static Request ofString(final String url, final RequestMethod method) {
        try {
            return new Request(new URL(url), new Headers(), method, null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL url;
    private Headers headers;
    private RequestMethod method;
    private @Nullable RequestWriter writer;

    public Request(final URL url, final Headers headers, final RequestMethod method, @Nullable final RequestWriter writer) {
        this.url = url;
        this.headers = headers;
        this.method = method;
        this.writer = writer;
    }

    public Request withUrl(final URL url) {
        setUrl(url);
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

    public void setUrl(final URL url) {
        this.url = url;
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

    public URL url() {
        return url;
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
}
