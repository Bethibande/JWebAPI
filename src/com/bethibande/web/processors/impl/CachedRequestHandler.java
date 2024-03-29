package com.bethibande.web.processors.impl;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.JWebServer;
import com.bethibande.web.types.CacheType;
import com.bethibande.web.types.Request;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.annotations.CacheRequest;
import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotatedInvocationHandler;
import com.bethibande.web.types.MetaData;

import java.lang.reflect.Method;

public class CachedRequestHandler extends AnnotatedInvocationHandler<CacheRequest> {

    public CachedRequestHandler() {
        super(CacheRequest.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void beforeInvocation(Method method, CacheRequest annotation, Request _request, JWebAPI api) {
        if(!(_request instanceof WebRequest request)) throw new RuntimeException("Not yet supported by client");

        String path = request.getUri().getPath();
        if(annotation.global()) {
            CachedRequest cachedRequest = api.getRequestCache().get(path);
            if(cachedRequest == null) return;

            request.setResponse(cachedRequest.getResponse());
            request.setFinished(true);
            return;
        }

        if(!(api instanceof JWebServer)) {
            throw new RuntimeException("Local cache not supported by client.");
        }

        ServerContext context = LocalServerContext.getContext();
        MetaData localMetadata = context.session().getMeta();
        if(!localMetadata.hasMeta("localSessionCache")) return;

        Cache<String, CachedRequest> requestCache = localMetadata.getAsType("localSessionCache", Cache.class);

        boolean update = requestCache.update();
        if(update) api.getLogger().finest("Local Cache Update");

        CachedRequest cachedRequest = requestCache.get(path);
        if(cachedRequest == null) return;

        request.setResponse(cachedRequest.getResponse());
        request.setFinished(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterInvocation(Method method, CacheRequest annotation, Request _request, JWebAPI api) {
        if(!(_request instanceof WebRequest request)) throw new RuntimeException("Not yet supported by client");

        String path = request.getUri().getPath();
        CachedRequest cachedRequest = new CachedRequest(path, request.getResponse());
        if(annotation.global()) {

            if(annotation.cacheTime() == 0) api.getRequestCache().put(path, cachedRequest);
            if(annotation.cacheTime() > 0) api.getRequestCache().put(path, cachedRequest, annotation.cacheTime());

            return;
        }

        if(!(api instanceof JWebServer server)) {
            throw new RuntimeException("Local cache not supported by client.");
        }

        ServerContext context = LocalServerContext.getContext();
        MetaData localMetadata = context.session().getMeta();
        if(!localMetadata.hasMeta("localSessionCache")) {
            localMetadata.set("localSessionCache", server
                    .getCacheSupplier()
                    .getRequestCache(
                            server,
                            server.getCacheConfig(),
                            CacheType.LOCAL_REQUEST_CACHE
                    )
            );
        }

        Cache<String, CachedRequest> requestCache = localMetadata.getAsType("localSessionCache", Cache.class);

        if(annotation.cacheTime() == 0) requestCache.put(path, cachedRequest);
        if(annotation.cacheTime() > 0) requestCache.put(path, cachedRequest, annotation.timeUnit().toMillis(annotation.cacheTime()));
    }
}
