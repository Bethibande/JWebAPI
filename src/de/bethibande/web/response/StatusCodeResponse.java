package de.bethibande.web.response;

import java.util.HashMap;

public class StatusCodeResponse implements ServerResponse {

    private final int statusCode;

    public StatusCodeResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public HashMap<String, String> getHeaders() {
        return null;
    }
}
