package com.bethibande.web.writers;

import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.types.RequestWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamWriter implements RequestWriter {

    private final InputStream stream;
    private final long length;
    private long offset = 0;

    public StreamWriter(final InputStreamWrapper wrapper) {
        this.stream = wrapper.getStream();
        this.length = wrapper.getLength();
    }

    public StreamWriter(final InputStream stream, final long length) {
        this.stream = stream;
        this.length = length;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public void reset() {
        offset = 0;
        try {
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return offset < length;
    }

    @Override
    public void write(final OutputStream stream, final int bufferSize) throws IOException {
        final int copy = (int) Math.min(bufferSize, length - offset);

        stream.write(this.stream.readNBytes(copy));
    }
}
