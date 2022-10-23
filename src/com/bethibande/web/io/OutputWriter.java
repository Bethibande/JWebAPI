package com.bethibande.web.io;

import com.bethibande.web.types.WebRequest;
import com.bethibande.web.response.RequestResponse;

import java.io.IOException;

public interface OutputWriter {

    void write(WebRequest request, RequestResponse response) throws IOException;

}
