package com.bethibande.web.response;

import java.io.InputStream;
import java.util.HashMap;

public interface ServerResponse {

    int getStatusCode();
    HashMap<String, String> getHeaders();

    static ServerResponse httpStatusCode(int code) {
        return new StatusCodeResponse(code);
    }

    static ServerResponse redirect(String url) {
        return new RedirectResponse(url);
    }

    static StreamResponse stream(InputStream stream, long length) {
        return new StreamResponse("text/plain", stream, length);
    }
    static StreamResponse stream(InputStream stream, String contentType, long length) {
        return new StreamResponse(contentType, stream, length);
    }

}
