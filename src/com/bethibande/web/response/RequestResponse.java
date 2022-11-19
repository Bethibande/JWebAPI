package com.bethibande.web.response;

import com.sun.net.httpserver.Headers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class RequestResponse {

    public static RequestResponse redirect(String redirect) {
        return new RequestResponse()
                .withLocation(redirect)
                .withStatusCode(301);
    }

    public static RequestResponse stream(InputStream stream) {
        try {
            return stream(stream, stream.available());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestResponse stream(InputStream stream, long length) {
        return new RequestResponse()
                .withStatusCode(202)
                .withContentLength(length)
                .withContentData(new InputStreamWrapper(stream, length));
    }

    public static RequestResponse file(File file) {
        try {
            InputStream in = new FileInputStream(file);

            return stream(in, file.length());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestResponse build() {
        return new RequestResponse();
    }

    private int statusCode = 200;

    private Headers header = new Headers();

    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Can handle <br>
     * String <br>
     * byte[] <br>
     * InputStream in, contentLength will be equals {@link InputStream#available()} unless contentLength is set using {@link #setContentLength(long)}<br>
     * Object, will be serialized as json text
     */
    private Object contentData;

    /**
     * @see #setCookie(String, String, long)
     */
    public RequestResponse withCookie(String cookie, String value, long expirationDate) {
        setCookie(cookie, value, expirationDate);
        return this;
    }

    /**
     * @see #setCookie(String, String)
     */
    public RequestResponse withCookie(String cookie, String value) {
        setCookie(cookie, value);
        return this;
    }

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

    public RequestResponse withHeader(Headers header) {
        setHeader(header);
        return this;
    }

    public RequestResponse withHeader(String key, String value) {
        addHeader(key, value);
        return this;
    }

    public RequestResponse withHeader(String key, Object value) {
        addHeader(key, value.toString());
        return this;
    }

    /**
     * Set the Set-Cookie header, can create multiple Set-Cookie headers.
     * Set the value to null, inorder to delete a cookie
     * @param cookie the cookie name
     * @param value the value of your cookie, null to delete cookie
     * @param expirationDate unix timestamp in s, expiration date of your cookie
     */
    public void setCookie(String cookie, String value, long expirationDate) {
        if(hasCookie(cookie)) deleteCookie(cookie);
        if(value == null) return;

        addHeader("Set-Cookie", cookie + "=" + value + "; Expires=" + new Date(expirationDate));
    }

    /**
     * Set the Set-Cookie header, can create multiple Set-Cookie headers.
     * Set the value to null, inorder to delete a cookie
     * @param cookie the cookie name
     * @param value the value of your cookie, null to delete cookie
     */
    public void setCookie(String cookie, String value) {
        if(hasCookie(cookie)) deleteCookie(cookie);
        if(value == null) return;

        addHeader("Set-Cookie", cookie + "=" + value + ";");
    }

    public void deleteCookie(String cookie) {
        List<String> list = header.get("Set-Cookie").stream().filter(str -> !str.toLowerCase().startsWith(cookie.toLowerCase())).toList();

        header.remove("Set-Cookie");
        header.put("Set-Cookie", list);
    }

    public boolean hasCookie(String cookie) {
        return header.get("Set-Cookie") != null && header.get("Set-Cookie").stream().anyMatch(str -> str.toLowerCase().startsWith(cookie.toLowerCase()));
    }

    /**
     * Used when writing strings or objects
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setHeader(String key, String value) {
        header.remove(key);
        header.put(key, List.of(value));
    }

    public void addHeader(String key, String value) {
        header.add(key, value);
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

    public void setHeader(Headers header) {
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

    public Headers getHeader() {
        return header;
    }

    public String getContentType() {
        List<String> values = header.get("Content-Type");
        if(values == null || values.isEmpty()) return null;
        return values.get(0);
    }

    public String getLocation() {
        List<String> values = header.get("Location");
        if(values == null || values.isEmpty()) return null;
        return values.get(0);
    }

    public long getContentLength() {
        List<String> values = header.get("Content-Length");
        if(values == null || values.isEmpty()) return 0;
        return Long.parseLong(values.get(0));
    }

    public Object getContentData() {
        return contentData;
    }
}
