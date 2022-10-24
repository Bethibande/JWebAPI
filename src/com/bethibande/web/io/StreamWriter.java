package com.bethibande.web.io;

import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.types.WebRequest;

import java.io.IOException;

public class StreamWriter implements OutputWriter {

    @Override
    public void write(WebRequest request, RequestResponse response) throws IOException {
        if(!(response.getContentData() instanceof InputStreamWrapper wrapper)) throw new RuntimeException("Invalid type");

        wrapper.write(request);
    }
}
