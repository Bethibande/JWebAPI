package com.bethibande.web.response;

import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.types.WebRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("unused")
public class InputStreamWrapper {

    private final InputStream stream;
    private final Long length;

    public InputStreamWrapper(InputStream stream) {
        this.stream = stream;
        try {
            length = (long) stream.available();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStreamWrapper(InputStream stream, Long length) {
        this.stream = stream;
        this.length = length;
    }

    public InputStream getStream() {
        return stream;
    }

    public Long getLength() {
        return length;
    }

    public void write(WebRequest request) {
        try {
            ServerContext context = LocalServerContext.getContext();
            if(context == null) throw new RuntimeException("Cannot write InputStreamWrapper without context.");

            OutputStream out = request.getExchange().getResponseBody();

            byte[] buffer = new byte[context.metadata().getBufferSize()];
            long total = length;
            int read;
            while((read = stream.read(buffer)) > 0) {
                out.write(buffer, 0, read);
                out.flush();

                total -= read;
                if(total <= 0) break;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
