package com.bethibande.web.cache;

public class CacheConfig {

    private CacheLifetimeType lifetimeType;
    private long maxLifetime;
    private int maxItems;

    public CacheConfig() {
        this.lifetimeType = CacheLifetimeType.ON_CREATION;
        this.maxItems = Integer.MAX_VALUE;
        this.maxLifetime = 60000L;
    }

    public CacheConfig(CacheLifetimeType lifetimeType, long maxLifetime, int maxItems) {
        this.lifetimeType = lifetimeType;
        this.maxLifetime = maxLifetime;
        this.maxItems = maxItems;
    }

    public CacheConfig withLifetimeType(CacheLifetimeType lifetimeType) {
        setLifetimeType(lifetimeType);
        return this;
    }

    public CacheConfig withMaxLifetime(long maxLifetime) {
        setMaxLifetime(maxLifetime);
        return this;
    }

    public CacheConfig withMaxItems(int maxItems) {
        setMaxItems(maxItems);
        return this;
    }

    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public void setLifetimeType(CacheLifetimeType lifetimeType) {
        this.lifetimeType = lifetimeType;
    }

    public CacheLifetimeType getLifetimeType() {
        return lifetimeType;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public int getMaxItems() {
        return maxItems;
    }
}
