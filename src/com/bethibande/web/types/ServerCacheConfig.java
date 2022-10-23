package com.bethibande.web.types;

import com.bethibande.web.cache.CacheConfig;

public class ServerCacheConfig {

    private CacheConfig sessionCacheConfig;
    private CacheConfig globalRequestCacheConfig;
    private CacheConfig localRequestCacheConfig;

    public ServerCacheConfig(CacheConfig sessionCacheConfig, CacheConfig globalRequestCacheConfig, CacheConfig localRequestCacheConfig) {
        this.sessionCacheConfig = sessionCacheConfig;
        this.globalRequestCacheConfig = globalRequestCacheConfig;
        this.localRequestCacheConfig = localRequestCacheConfig;
    }

    public void setSessionCacheConfig(CacheConfig sessionCacheConfig) {
        this.sessionCacheConfig = sessionCacheConfig;
    }

    public void setGlobalRequestCacheConfig(CacheConfig globalRequestCacheConfig) {
        this.globalRequestCacheConfig = globalRequestCacheConfig;
    }

    public void setLocalRequestCacheConfig(CacheConfig localRequestCacheConfig) {
        this.localRequestCacheConfig = localRequestCacheConfig;
    }

    public CacheConfig getSessionCacheConfig() {
        return sessionCacheConfig;
    }

    public CacheConfig getGlobalRequestCacheConfig() {
        return globalRequestCacheConfig;
    }

    public CacheConfig getLocalRequestCacheConfig() {
        return localRequestCacheConfig;
    }
}
