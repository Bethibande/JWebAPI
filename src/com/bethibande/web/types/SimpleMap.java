package com.bethibande.web.types;

import com.bethibande.web.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;

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

    /**
     * Insert value into map using binary search. Insert objects from the biggest value to the smallest
     * where the biggest value is at index 0 and the smallest at index n.
     * @param intMapper function to map keys to integer values used to compare keys
     * @param key key to insert
     * @param value value to insert
     */
    public void searchInsert(final Function<K, Integer> intMapper, final K key, final V value) {
        int l = 0;
        int r = keys.length;

        final int target = intMapper.apply(key);
        int insertAt = 0;

        while(true) {
            if(keys.length == 0) {
                break;
            }

            final int middle = (int)((r - l) / 2.0);
            final int _value = intMapper.apply(keys[l + middle]);

            if(_value == target) {
                insertAt = l + middle;
                break;
            }
            if(middle == 0) {
                if(target > _value) {
                    insertAt = l + middle;
                    break;
                }
                insertAt = l + middle + 1;
                break;
            }
            if(_value > target) {
                l += middle;
            }
            if(_value < target) {
                r -= middle;
            }
        }

        putAtIndex(insertAt, key, value);
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

    /**
     * Does the same as the {@link #put(Object, Object)} method but instead of inserting the key/value pair at the end of the map,
     * inserts them at the given index, index may not be bigger then the size of the map
     * @param index index to insert the value at
     * @param key key to insert
     * @param value value to insert
     */
    public void putAtIndex(final int index, K key, V value) {
        remove(key);

        K[] oldKeys = keys;
        V[] oldValues = values;

        changeArraySize(keys.length + 1);

        if(oldKeys.length != 0) {
            System.arraycopy(oldKeys, 0, keys, 0, index);
            System.arraycopy(oldKeys, index, keys, index+1, oldKeys.length-index);
            System.arraycopy(oldValues, 0, values, 0, index);
            System.arraycopy(oldValues, index, values, index+1, oldValues.length-index);
        }

        keys[index] = key;
        values[index] = value;
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
