package com.bethibande.web.cache;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheTest {

    @Test
    public void maxLifetimeTest() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>()
                .withMaxLifetime(200L)
                .withLifetimeType(CacheLifetimeType.ON_CREATION);

        cache.put("a", "a");
        assertEquals(1, cache.getAllKeys().size());

        Thread.sleep(201L);

        cache.update();
        assertEquals(0, cache.getAllKeys().size());
    }

    @Test
    public void maxItemTest() {
        Cache<String, String> cache = new Cache<String, String>()
                .withMaxItems(2);

        cache.put("a", "a");
        cache.put("b", "b");
        cache.put("c", "c");

        assertEquals(2, cache.getAllKeys().size());
    }

}
