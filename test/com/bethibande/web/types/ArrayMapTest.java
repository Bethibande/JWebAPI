package com.bethibande.web.types;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayMapTest {

    @Test
    public void testMap() {
        final ArrayMap<String, String> map = new ArrayMap<>(String.class, String.class);

        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.isEmpty());

        map.put("a", "k");
        map.put("b", "k");
        map.put("c", "k");

        assertFalse(map.isEmpty());
        assertTrue(map.isNotEmpty());

        assertEquals(3, map.size());

        assertEquals(Set.of("a", "b", "c"), map.keySet());
        assertEquals(List.of("k", "k", "k"), map.values());

        map.remove("b");

        assertEquals(Set.of("a", "c"), map.keySet());

        final ArrayMap<String, String> map2 = new ArrayMap<>(String.class, String.class);
        map2.put("b", "k");

        map.putAll(map2);
        assertEquals(Set.of("a", "c", "b"), map.keySet());

        map.replace("a", "c", "k2");
        assertEquals(List.of("k", "k", "k"), map.values());

        map.replace("a", "k", "k2");
        assertEquals(List.of("k2", "k", "k"), map.values());

        final AtomicInteger i = new AtomicInteger();
        map.forEach((k, v) -> i.incrementAndGet());
        assertEquals(3, i.get());

        map.putIfAbsent("a", "k");
        assertEquals(List.of("k2", "k", "k"), map.values());

        map.putIfAbsent("d", "k");
        assertEquals(Set.of("a", "c", "b", "d"), map.keySet());

        String oldValue = map.put("a", "k");
        assertEquals("k2", oldValue);
        assertEquals(List.of("k", "k", "k", "k"), map.values());

        ArrayMap<String, String> map3 = new ArrayMap<>(new String[]{"key"}, new String[]{"value"});
        assertEquals(1, map3.size());

        ArrayMap<String, String> map4 = new ArrayMap<>(String[]::new, String[]::new);
        map4.put("a", "b");
        assertEquals(1, map4.size());
        assertEquals("b", map4.get("a"));

        assertEquals(Set.of("a", "c", "b", "d"), map.keySet());
        map.sort((e1, e2) -> {return e1.getKey().compareToIgnoreCase(e2.getKey());});
        assertEquals(Set.of("a", "b", "c", "d"), map.keySet());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.isNotEmpty());
    }

}
