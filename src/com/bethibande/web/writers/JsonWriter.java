package com.bethibande.web.writers;

import com.bethibande.web.types.RequestWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class JsonWriter implements RequestWriter {

    private final byte[] data;

    private boolean state = true;

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

    @Override
    public void reset() {
        state = true;
    }

    @Override
    public boolean hasNext() {
        return state;
    }

    @Override
    public void write(final OutputStream stream, final int bufferSize) throws IOException {
        stream.write(data);
        state = false;
    }
}
