package com.bethibande.web;

import com.bethibande.web.cache.Cache;
import com.bethibande.web.cache.CachedRequest;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.types.HasCharset;
import com.bethibande.web.types.HasExecutor;
import com.bethibande.web.types.HasGson;
import com.bethibande.web.types.HasLogger;

import java.util.List;

public interface JWebAPI extends HasExecutor, HasLogger, HasCharset, HasGson {

    Cache<String, CachedRequest> getRequestCache();

    JWebAPI withMethodInvocationHandler(MethodInvocationHandler handler);
    void registerMethodInvocationHandler(MethodInvocationHandler handler);
    List<MethodInvocationHandler> getMethodInvocationHandlers();

}
