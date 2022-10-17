package com.bethibande.web.io;

import com.bethibande.web.WebRequest;
import com.bethibande.web.response.RequestResponse;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayWriter implements OutputWriter {

    @Override
    public void write(WebRequest request, RequestResponse response) throws IOException {
        OutputStream out = request.getExchange().getResponseBody();

        out.write((byte[]) response.getContentData());
        out.flush();
    }
}
