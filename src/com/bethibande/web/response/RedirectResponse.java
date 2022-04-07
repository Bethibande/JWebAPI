package com.bethibande.web.response;

import java.util.HashMap;

public class RedirectResponse implements ServerResponse {

    private final String url;

    public RedirectResponse(String url) {
        this.url = url;
    }

    @Override
    public int getStatusCode() {
        return 307;
    }

    @Override
    public HashMap<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Location", url);
        return map;
    }
}
