package com.bethibande.web.processors.impl;

import com.bethibande.web.JWebServer;
import com.bethibande.web.WebRequest;
import com.bethibande.web.annotations.CacheRequest;
import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CacheLifetimeType;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.sessions.MetaData;

import java.lang.reflect.Method;

public class CachedRequestHandler extends AnnotatedInvocationHandler<CacheRequest> {

    public CachedRequestHandler() {
        super(CacheRequest.class);
    }

    @Override
    public void beforeInvocation(Method method, CacheRequest annotation, WebRequest request, JWebServer server) {
        String path = request.getUri().getPath();
        if(annotation.global()) {
            CachedRequest cachedRequest = server.getGlobalRequestCache().get(path);
            if(cachedRequest == null) return;

            request.setResponse(cachedRequest.getResponse());
            request.setFinished(true);
            return;
        }

        ServerContext context = LocalServerContext.getContext();
        MetaData localMetadata = context.session().getMeta();
        if(!localMetadata.hasMeta("localSessionCache")) return;

        Cache<String, CachedRequest> requestCache = localMetadata.getAsType("localSessionCache", Cache.class);
        long lastUpdate = localMetadata.getLong("lastLocalSessionCacheUpdate");

        if(System.currentTimeMillis() - 1000L > lastUpdate) {
            requestCache.update();
            localMetadata.set("lastLocalSessionCacheUpdate", System.currentTimeMillis());
        }

        CachedRequest cachedRequest = requestCache.get(path);
        if(cachedRequest == null) return;

        request.setResponse(cachedRequest.getResponse());
        request.setFinished(true);
    }

    @Override
    public void afterInvocation(Method method, CacheRequest annotation, WebRequest request, JWebServer server) {
        String path = request.getUri().getPath();
        CachedRequest cachedRequest = new CachedRequest(path, request.getResponse());
        if(annotation.global()) {

            if(annotation.cacheTime() == 0) server.getGlobalRequestCache().put(path, cachedRequest);
            if(annotation.cacheTime() > 0) server.getGlobalRequestCache().put(path, cachedRequest, annotation.cacheTime());

            return;
        }

        ServerContext context = LocalServerContext.getContext();
        MetaData localMetadata = context.session().getMeta();
        if(!localMetadata.hasMeta("localSessionCache")) {
            localMetadata.set("localSessionCache", new Cache<>()
                    .withLifetimeType(CacheLifetimeType.ON_CREATION)
                    .withMaxLifetime(60000L));
            localMetadata.set("lastLocalSessionCacheUpdate", 0L);
        }

        Cache<String, CachedRequest> requestCache = localMetadata.getAsType("localSessionCache", Cache.class);

        if(annotation.cacheTime() == 0) requestCache.put(path, cachedRequest);
        if(annotation.cacheTime() > 0) requestCache.put(path, cachedRequest, annotation.cacheTime());
    }
}
