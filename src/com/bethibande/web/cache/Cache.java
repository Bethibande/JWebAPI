package com.bethibande.web.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Cache<K, V> {

    private final HashMap<K, CacheItem<K, V>> cache = new HashMap<>();

    private long maxLifetime;
    private int maxItems = Integer.MAX_VALUE;
    private CacheLifetimeType lifetimeType;

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

        for(CacheItem<K, V> item : cache.values()) {
            if(item.getExpirationDate() < date) {
                date = item.getExpirationDate();
                oldest = item.getKey();
            }
        }

        cache.remove(oldest);
    }

    /**
     * Updates the cache, removes items if the cache exceeds the maxItems size and removes expired entries.
     */
    public void update() {
        if(cache.size() > maxItems) {
            makeSpace();
        }

        Collection<K> expired = new ArrayList<>();
        for(CacheItem<K, V> item : cache.values()) {
            if(item.getExpirationDate() <= System.currentTimeMillis()) {
                expired.add(item.getKey());
            }
        }

        expired.forEach(this::remove);
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
