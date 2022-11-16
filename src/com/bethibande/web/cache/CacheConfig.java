package com.bethibande.web.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class CacheConfig {

    private CacheLifetimeType lifetimeType;
    private long maxLifetime;
    private int maxItems;
    private long updateTimeout;

    public CacheConfig() {
        this.lifetimeType = CacheLifetimeType.ON_CREATION;
        this.maxItems = Integer.MAX_VALUE;
        this.maxLifetime = 60000L;
        this.updateTimeout = 1000L;
    }

    public CacheConfig(@NotNull CacheLifetimeType lifetimeType,
                       @Range(from = 0, to = Long.MAX_VALUE) long maxLifetime,
                       @Range(from = 0, to = Integer.MAX_VALUE) int maxItems,
                       @Range(from = 0, to = Long.MAX_VALUE) long updateTimeout) {
        this.lifetimeType = lifetimeType;
        this.maxLifetime = maxLifetime;
        this.maxItems = maxItems;
        this.updateTimeout = updateTimeout;
    }

    public CacheConfig withUpdateTimeout(@Range(from = 0, to = Long.MAX_VALUE) long updateTimeout) {
        setUpdateTimeout(updateTimeout);
        return this;
    }

    public CacheConfig withLifetimeType(@NotNull CacheLifetimeType lifetimeType) {
        setLifetimeType(lifetimeType);
        return this;
    }

    public CacheConfig withMaxLifetime(@Range(from = 0, to = Long.MAX_VALUE) long maxLifetime) {
        setMaxLifetime(maxLifetime);
        return this;
    }

    public CacheConfig withMaxItems(@Range(from = 0, to = Integer.MAX_VALUE) int maxItems) {
        setMaxItems(maxItems);
        return this;
    }

    public void setUpdateTimeout(@Range(from = 0, to = Long.MAX_VALUE) long updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

    public void setMaxLifetime(@Range(from = 0, to = Long.MAX_VALUE) long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public void setMaxItems(@Range(from = 0, to = Integer.MAX_VALUE) int maxItems) {
        this.maxItems = maxItems;
    }

    public void setLifetimeType(@NotNull CacheLifetimeType lifetimeType) {
        this.lifetimeType = lifetimeType;
    }

    public @NotNull CacheLifetimeType getLifetimeType() {
        return lifetimeType;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public long getUpdateTimeout() {
        return updateTimeout;
    }

}
