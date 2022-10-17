package com.bethibande.web.cache;

import java.util.UUID;

public class CachedRequest {

    private String uri;
    private UUID requester;
    private Object response;
    private Long creationDate;

}
