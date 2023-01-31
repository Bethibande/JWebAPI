package com.bethibande.web.writers;

import com.bethibande.web.types.RequestWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class JsonWriter implements RequestWriter {

    private final byte[] data;

    public JsonWriter(final String json, final Charset charset) {
        this(json.getBytes(charset));
    }

    public JsonWriter(final byte[] data) {
        this.data = data;
    }

    @Override
    public long getLength() {
        return data.length;
    }

    /**
     * @throws UnsupportedOperationException always throws this exception, class does not support reset operation.
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Operation not supported by json writer");
    }

    /**
     * Always returns false. Class always writes all data in one go
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void write(final OutputStream stream, final int bufferSize) throws IOException {
        stream.write(data);
    }
}
