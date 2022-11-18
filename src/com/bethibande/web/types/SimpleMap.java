package com.bethibande.web.types;

import com.bethibande.web.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class SimpleMap<K, V> implements Iterable<K> {

    private final Class<K> keyType;
    private final Class<V> valueType;

    private K[] keys;
    private V[] values;

    public SimpleMap(Class<K> keyType, Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;

        changeArraySize(0);
    }

    @NotNull
    @Override
    public Iterator<K> iterator() {
        return ArrayIterator.of(keys);
    }

    private void changeArraySize(int size) {
        keys = ArrayUtils.createArray(keyType, size);
        values = ArrayUtils.createArray(valueType, size);
    }

    public int indexOf(K key) {
        for(int i = 0; i < keys.length; i++) {
            if(keys[i].equals(key)) return i;
        }
        return -1;
    }


    public void remove(K key) {
        int index = indexOf(key);
        if(index == -1) return;

        K[] oldKeys = keys;
        V[] oldValues = values;

        changeArraySize(keys.length - 1);

        if(keys.length != 0) {
            ArrayUtils.rangeCopy(oldKeys, keys, index, 1);
            ArrayUtils.rangeCopy(oldValues, values, index, 1);
        }
    }

    public V get(K key) {
        int index = indexOf(key);
        if(index == -1) return null;

        return values[index];
    }

    public void put(K key, V value) {
        remove(key);

        K[] oldKeys = keys;
        V[] oldValues = values;

        changeArraySize(keys.length + 1);

        if(oldKeys.length != 0) {
            System.arraycopy(oldKeys, 0, keys, 0, oldKeys.length);
            System.arraycopy(oldValues, 0, values, 0, oldValues.length);
        }

        keys[keys.length-1] = key;
        values[values.length-1] = value;
    }

}
