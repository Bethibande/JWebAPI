package com.bethibande.web.cache;

public class CacheItem<K, V> {

    private final K key;
    private final V value;
    private Long expirationDate;

    public CacheItem(K key, V value, Long expirationDate) {
        this.key = key;
        this.value = value;
        this.expirationDate = expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }
}
