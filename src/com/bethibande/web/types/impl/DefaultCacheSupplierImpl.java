package com.bethibande.web.types.impl;

import com.bethibande.web.JWebServer;
import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.CacheType;
import com.bethibande.web.types.ServerCacheConfig;
import com.bethibande.web.types.ServerCacheSupplier;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultCacheSupplierImpl implements ServerCacheSupplier {

    private final Supplier<Cache<UUID, Session>> sessionCacheSupplier;
    private final Supplier<Cache<String, CachedRequest>> requestCacheSupplier;

    public DefaultCacheSupplierImpl(
            Supplier<Cache<UUID, Session>> sessionCacheSupplier,
            Supplier<Cache<String, CachedRequest>> requestCacheSupplier
    ) {
        this.sessionCacheSupplier = sessionCacheSupplier;
        this.requestCacheSupplier = requestCacheSupplier;
    }

    @Override
    public Cache<UUID, Session> getSessionCache(JWebServer server, ServerCacheConfig config) {
        return sessionCacheSupplier
                .get()
                .apply(config.getSessionCacheConfig());
    }

    @Override
    public Cache<String, CachedRequest> getRequestCache(JWebServer server, ServerCacheConfig config, CacheType type) {
        return switch (type) {
            case LOCAL_REQUEST_CACHE -> requestCacheSupplier
                    .get()
                    .apply(config.getLocalRequestCacheConfig());
            case GLOBAL_REQUEST_CACHE -> requestCacheSupplier
                    .get()
                    .apply(config.getGlobalRequestCacheConfig());
        };
    }
}
