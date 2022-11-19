package com.bethibande.web.cache;

import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Cache<K, V> {

    private final HashMap<K, CacheItem<K, V>> cache = new HashMap<>();

    private long maxLifetime;
    private int maxItems = Integer.MAX_VALUE;
    private CacheLifetimeType lifetimeType;

    private long lastCacheUpdate = 0;
    private long cacheUpdateTimeout = 0;

    /**
     * Load config values
     * @param config the config to load
     * @return the current cache instance
     */
    public Cache<K, V> apply(CacheConfig config) {
        this.maxItems = config.getMaxItems();
        this.maxLifetime = config.getMaxLifetime();
        this.lifetimeType = config.getLifetimeType();
        this.cacheUpdateTimeout = config.getUpdateTimeout();

        return this;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public CacheLifetimeType getLifetimeType() {
        return lifetimeType;
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

    public Cache<K, V> withMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }

    public Cache<K, V> withMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public Cache<K, V> withLifetimeType(CacheLifetimeType lifetimeType) {
        this.lifetimeType = lifetimeType;
        return this;
    }

    public boolean hasKey(K key) {
        return cache.containsKey(key);
    }

    public void remove(K key) {
        cache.remove(key);
    }

    /**
     * Removes the oldest item from cache
     */
    public void makeSpace() {
        long date = Long.MAX_VALUE;
        K oldest = null;

        for (CacheItem<K, V> item : cache.values()) {
            if (item.getExpirationDate() < date) {
                date = item.getExpirationDate();
                oldest = item.getKey();
            }
        }

        cache.remove(oldest);
    }

    /**
     * Sets a timeout for the update method, no matter how often the method is called, it will only be executed after the timeout has passed.
     * @param timeout timeout in the specified time unit, will be converted to ms
     * @param timeUnit timeunit for timeout parameter
     * @see #setCacheUpdateTimeout(long)
     * @see #update()
     */
    public void setCacheUpdateTimeout(@Range(from = 0, to = Long.MAX_VALUE) long timeout, TimeUnit timeUnit) {
        setCacheUpdateTimeout(timeUnit.toMillis(timeout));
    }

    /**
     * Sets a timeout for the update method, no matter how often the method is called, it will only be executed after the timeout has passed.
     * @param timeout timeout time in ms
     * @see #setCacheUpdateTimeout(long, TimeUnit)
     * @see #update()
     */
    public void setCacheUpdateTimeout(@Range(from = 0, to = Long.MAX_VALUE) long timeout) {
        this.cacheUpdateTimeout = timeout;
    }

    /**
     * Get the cache update method timeout value, for more details, see {@link #setCacheUpdateTimeout(long)} or {@link #update()}
     * @return timeout time in ms
     */
    public long getCacheUpdateTimeout() {
        return cacheUpdateTimeout;
    }

    /**
     * Updates the cache, removes items if the cache exceeds the maxItems size and removes expired entries.
     * If cacheUpdateTimeout value is set, cacheUpdateTimeout amount of time will have to pass before the next update() call.
     * If update() is called before the timeout has passed, false will be returned.
     * @return true if cache was updated
     * @see #setCacheUpdateTimeout(long)
     */
    public boolean update() {
        final long currentTimeMillis = System.currentTimeMillis();
        if(cacheUpdateTimeout != 0 && lastCacheUpdate + cacheUpdateTimeout > currentTimeMillis) {
            return false;
        }

        if(cache.size() > maxItems) {
            makeSpace();
        }

        cache.entrySet().removeIf(entry -> entry.getValue().getExpirationDate() <= currentTimeMillis);

        lastCacheUpdate = currentTimeMillis;

        return true;
    }

    public void put(K key, V value) {
        put(key, value, this.maxLifetime);
    }

    public void put(K key, V value, long maxLifetime) {
        remove(key);
        this.cache.put(key, new CacheItem<>(key, value, System.currentTimeMillis() + maxLifetime));

        update();
    }

    public V get(K key) {
        CacheItem<K, V> item = cache.get(key);
        if(item == null) return null;

        if(lifetimeType.equals(CacheLifetimeType.ON_ACCESS)) {
            item.setExpirationDate(System.currentTimeMillis() + maxLifetime);
        }

        return item.getValue();
    }

    public Collection<K> getAllKeys() {
        return cache.keySet();
    }

}
