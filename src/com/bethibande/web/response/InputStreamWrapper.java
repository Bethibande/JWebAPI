package com.bethibande.web.response;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.WebRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            OutputStream out = request.getExchange().getResponseBody();

            byte[] buffer = new byte[1024];
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
