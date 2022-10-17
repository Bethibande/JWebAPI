package com.bethibande.web.cache;

public enum CacheLifetimeType {

    /**
     * This will set the items lifetime when it is created and never update it again.
     * The item will be removed from cache when its lifetime is up, no matter how often it was accessed.
     */
    ON_CREATION,
    /**
     * This will set the items lifetime when it is created and reset its lifetime whenever it is accessed.
     * The item will be removed from cache, only if it has not been used in a long time.
     */
    ON_ACCESS

}
