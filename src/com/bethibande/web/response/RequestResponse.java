package com.bethibande.web.response;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RequestResponse {

    public static RequestResponse build() {
        return new RequestResponse();
    }

    private int statusCode = 200;

    private HashMap<String, String> header = new HashMap<>();

    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Can handle <br>
     * String <br>
     * byte[] <br>
     * InputStream in, contentLength will be equals {@link InputStream#available()} unless contentLength is set using {@link #setContentLength(long)}<br>
     * Object, will be serialized as json text
     */
    private Object contentData;

    public RequestResponse withCharset(Charset charset) {
        setCharset(charset);
        return this;
    }

    public RequestResponse withLocation(String redirect) {
        setLocation(redirect);
        return this;
    }

    public RequestResponse withContentType(String contentType) {
        setContentType(contentType);
        return this;
    }

    public RequestResponse withContentLength(long length) {
        setContentLength(length);
        return this;
    }

    public RequestResponse withStatusCode(int statusCode) {
        setStatusCode(statusCode);
        return this;
    }

    public RequestResponse withContentData(Object data) {
        setContentData(data);
        return this;
    }

    public RequestResponse withHeader(HashMap<String, String> header) {
        setHeader(header);
        return this;
    }

    public RequestResponse withHeader(String key, String value) {
        setHeader(key, value);
        return this;
    }

    public RequestResponse withHeader(String key, Object value) {
        setHeader(key, value.toString());
        return this;
    }

    /**
     * Used when writing strings or objects
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setHeader(String key, String value) {
        header.remove(key);
        header.put(key, value);
    }

    public void setLocation(String redirect) {
        setHeader("Location", redirect);
    }

    public void setContentType(String type) {
        setHeader("Content-Type", type);
    }

    public void setContentLength(long length) {
        setHeader("Content-Length", String.valueOf(length));
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeader(HashMap<String, String> header) {
        this.header = header;
    }

    public void setContentData(Object contentData) {
        this.contentData = contentData;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Charset getCharset() {
        return charset;
    }

    public HashMap<String, String> getHeader() {
        return header;
    }

    public String getContentType() {
        return header.get("Content-Type");
    }

    public String getLocation() {
        return header.get("Location");
    }

    public long getContentLength() {
        String str = header.get("Content-Length");
        if(str == null) return 0;

        return Long.parseLong(str);
    }

    public Object getContentData() {
        return contentData;
    }
}
