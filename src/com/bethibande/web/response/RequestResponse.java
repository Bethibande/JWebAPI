package com.bethibande.web.response;

import com.sun.net.httpserver.Headers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class RequestResponse {

    public static RequestResponse STATUS_OK = statusCode(200);
    public static RequestResponse STATUS_ACCEPTED = statusCode(202);
    public static RequestResponse STATUS_BAD_REQUEST = statusCode(400);
    public static RequestResponse STATUS_FORBIDDEN = statusCode(403);
    public static RequestResponse STATUS_NOT_FOUND = statusCode(404);
    public static RequestResponse STATUS_METHOD_NOT_ALLOWED = statusCode(405);
    public static RequestResponse STATUS_PAYLOAD_TOO_LARGE = statusCode(413);

    public static RequestResponse statusCode(final int statusCode) {
        return new RequestResponse().withStatusCode(statusCode);
    }

    /**
     * 301 redirect
     * @param redirect redirect url
     */
    public static RequestResponse redirect(final String redirect) {
        return new RequestResponse()
                .withLocation(redirect)
                .withStatusCode(301);
    }

    public static RequestResponse stream(final InputStream stream) {
        try {
            return stream(stream, stream.available());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestResponse stream(final InputStream stream, final long length) {
        return new RequestResponse()
                .withStatusCode(202)
                .withContentLength(length)
                .withContentData(new InputStreamWrapper(stream, length));
    }

    public static RequestResponse file(final File file) {
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

    private HttpURLConnection connection;

    /**
     * Can handle <br>
     * String <br>
     * byte[] <br>
     * InputStream in, contentLength will be equals {@link InputStream#available()} unless contentLength is set using {@link #setContentLength(long)}<br>
     * Object, will be serialized as json text
     */
    private Object contentData;

    /**
     * Used to terminate pending http client connection.
     * Method silently returns if connection is null.
     * Sets the connection to null
     * @see #withConnection(HttpURLConnection)
     */
    public void disconnect() {
        if(connection == null) return;
        connection.disconnect();
        connection = null;
    }

    /**
     * Set the connection that generated the response, only used to client responses.
     * @see #disconnect()
     */
    public RequestResponse withConnection(final HttpURLConnection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * @see #setCookie(String, String, long)
     */
    public RequestResponse withCookie(final String cookie, final String value, final long expirationDate) {
        setCookie(cookie, value, expirationDate);
        return this;
    }

    /**
     * @see #setCookie(String, String)
     */
    public RequestResponse withCookie(final String cookie, final String value) {
        setCookie(cookie, value);
        return this;
    }

    public RequestResponse withCharset(final Charset charset) {
        setCharset(charset);
        return this;
    }

    public RequestResponse withLocation(final String redirect) {
        setLocation(redirect);
        return this;
    }

    public RequestResponse withContentType(final String contentType) {
        setContentType(contentType);
        return this;
    }

    public RequestResponse withContentLength(final long length) {
        setContentLength(length);
        return this;
    }

    public RequestResponse withStatusCode(final int statusCode) {
        setStatusCode(statusCode);
        return this;
    }

    public RequestResponse withContentData(final Object data) {
        setContentData(data);
        return this;
    }

    public RequestResponse withHeader(final Headers header) {
        setHeader(header);
        return this;
    }

    public RequestResponse withHeader(final String key, final String value) {
        addHeader(key, value);
        return this;
    }

    public RequestResponse withHeader(final String key, final Object value) {
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
    public void setCookie(final String cookie, final String value, final long expirationDate) {
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
    public void setCookie(final String cookie, final String value) {
        if(hasCookie(cookie)) deleteCookie(cookie);
        if(value == null) return;

        addHeader("Set-Cookie", cookie + "=" + value + ";");
    }

    public void deleteCookie(final String cookie) {
        List<String> list = header.get("Set-Cookie").stream().filter(str -> !str.toLowerCase().startsWith(cookie.toLowerCase())).toList();

        header.remove("Set-Cookie");
        header.put("Set-Cookie", list);
    }

    public boolean hasCookie(final String cookie) {
        return header.get("Set-Cookie") != null && header.get("Set-Cookie").stream().anyMatch(str -> str.toLowerCase().startsWith(cookie.toLowerCase()));
    }

    /**
     * Used when writing strings or objects
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    public void setHeader(final String key, final String value) {
        header.remove(key);
        header.put(key, List.of(value));
    }

    public void addHeader(final String key, final String value) {
        header.add(key, value);
    }

    public void setLocation(final String redirect) {
        setHeader("Location", redirect);
    }

    public void setContentType(final String type) {
        setHeader("Content-Type", type);
    }

    public void setContentLength(final long length) {
        setHeader("Content-Length", String.valueOf(length));
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeader(final Headers header) {
        this.header = header;
    }

    public void setContentData(final Object contentData) {
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
        final String value = header.getFirst("Content-Length");
        if(value == null) return 0;
        return Long.parseLong(value);
    }

    public Object getContentData() {
        return contentData;
    }
}
