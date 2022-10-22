package com.bethibande.web.cache;

import com.bethibande.web.response.RequestResponse;

public class CachedRequest {

    private final String uri;
    private final RequestResponse response;
    private final Long creationDate;

    public CachedRequest(String uri, RequestResponse response) {
        this.uri = uri;
        this.response = response;
        this.creationDate = System.currentTimeMillis();
    }

    public CachedRequest(String uri, RequestResponse response, Long creationDate) {
        this.uri = uri;
        this.response = response;
        this.creationDate = creationDate;
    }

    public String getUri() {
        return uri;
    }

    public RequestResponse getResponse() {
        return response;
    }

    public Long getCreationDate() {
        return creationDate;
    }
}
