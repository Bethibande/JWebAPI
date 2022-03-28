package de.bethibande.web.response;

import java.io.InputStream;

public class StreamResponse {

    private final String contentType;
    private final InputStream stream;
    private final long length;

    public StreamResponse(String contentType, InputStream stream, long length) {
        this.contentType = contentType;
        this.stream = stream;
        this.length = length;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getStream() {
        return stream;
    }

    public long getLength() {
        return length;
    }
}
