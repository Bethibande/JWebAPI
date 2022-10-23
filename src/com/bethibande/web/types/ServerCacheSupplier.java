package com.bethibande.web.types;

import com.bethibande.web.JWebServer;
import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.sessions.Session;

import java.util.UUID;

public interface ServerCacheSupplier {

    Cache<UUID, Session> getSessionCache(JWebServer server, ServerCacheConfig config);
    Cache<String, CachedRequest> getRequestCache(JWebServer server, ServerCacheConfig config, CacheType type);
}
