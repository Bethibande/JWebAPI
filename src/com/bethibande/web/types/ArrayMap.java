package com.bethibande.web.types;

import com.bethibande.web.util.ArrayUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ArrayMap<K, V> implements Map<K, V> {

    private final Class<K> keyType;
    private final Class<V> valueType;

    private volatile K[] keys;
    private volatile V[] values;

    private final Function<Integer, K[]> keyArraySupplier;
    private final Function<Integer, V[]> valueArraySupplier;

    public ArrayMap(K[] keys, V[] values) {
        if(keys.length != values.length) {
            throw new RuntimeException("Input arrays must have the same length!");
        }

        this.keys = keys;
        this.values = values;

        this.keyType = (Class<K>)keys.getClass().arrayType();
        this.valueType = (Class<V>)values.getClass().arrayType();

        this.keyArraySupplier = length -> ArrayUtils.createArray(this.keyType, length);
        this.valueArraySupplier = length -> ArrayUtils.createArray(this.valueType, length);
    }

    public ArrayMap(Class<K> keyType, Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;

        this.keyArraySupplier = length -> ArrayUtils.createArray(this.keyType, length);
        this.valueArraySupplier = length -> ArrayUtils.createArray(this.valueType, length);
    }

    public ArrayMap(Class<K> keyType, Function<Integer, K[]> keyArraySupplier, Class<V> valueType, Function<Integer, V[]> valueArraySupplier) {
        this.keyType = keyType;
        this.valueType = valueType;

        this.keyArraySupplier = keyArraySupplier;
        this.valueArraySupplier = valueArraySupplier;
    }

    public void sort(Comparator<Entry<K, V>> comparator) {
        if(isEmpty()) return;

        //TODO: sort
    }

    @Override
    public int size() {
        return keys == null ? 0: keys.length;
    }

    @Override
    public V remove(Object key) {
        if(isEmpty()) return null;

        int index = indexOfKey(key);

        if(index != -1) {
            V value = values[index];

            K[] oldKeys = keys;
            V[] oldValues = values;

            setArrays(size() - 1);

            ArrayUtils.rangeCopy(oldKeys, keys, index, 1);
            ArrayUtils.rangeCopy(oldValues, values, index, 1);

            return value;
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isNotEmpty() {
        return size() > 0;
    }

    @Override
    public void clear() {
        keys = null;
        values = null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if(isEmpty()) return new LinkedHashSet<>();

        Set<Entry<K, V>> set = new LinkedHashSet<>();

        for(int i =  0; i < size(); i++) {
            set.add(getAsEntry(i));
        }

        return set;
    }

    /**
     * @return Immutable Set of keys, created using Set.of(K...)
     */
    @Override
    public Set<K> keySet() {
        if(isEmpty()) return new LinkedHashSet<>();
        return Set.of(keys);
    }

    /**
     * @return Immutable List of values, created using List.of(V...)
     */
    @Override
    public Collection<V> values() {
        if(isEmpty()) return new ArrayList<>();
        return List.of(values);
    }

    public Entry<K, V> getAsEntry(Object key) {
        if(isEmpty()) return null;
        int index = indexOfKey(key);
        if(index == -1) return null;
        return getAsEntry(index);
    }

    private Entry<K, V> getAsEntry(int index) {
        return new AbstractMap.SimpleImmutableEntry<>(keys[index], values[index]);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return Map.super.remove(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return Map.super.putIfAbsent(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if(isEmpty()) return false;

        V currentValue = get(key);
        if(currentValue == null && oldValue == null) {
            put(key, newValue);
            return true;
        }
        if(currentValue != null && currentValue.equals(oldValue)) {
            put(key, newValue);
            return true;
        }
        return false;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if(isEmpty()) return;

        for(int i = 0; i < size(); i++) {
            K key = keys[i];
            V newValue = function.apply(key, values[i]);
            put(key, newValue);
        }
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        if(isEmpty()) return;
        for(int i = 0; i < size(); i++) {
            action.accept(keys[i], values[i]);
        }
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if(isEmpty()) return defaultValue;
        if(!containsKey(key)) return defaultValue;
        return get(key);
    }

    @Override
    public V get(Object key) {
        if(isEmpty()) return null;
        int index = indexOfKey(key);
        if(index == -1) return null;

        return values[index];
    }

    private void setArrays(int length) {
        keys = keyArraySupplier.apply(length);
        values = valueArraySupplier.apply(length);
    }

    @Override
    public boolean containsKey(Object key) {
        if(isEmpty()) return false;
        for(K _key : keys) {
            if(_key == null && key == null) return true;
            if(_key != null && _key.equals(key)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if(isEmpty()) return false;
        for(V _value : values) {
            if(_value == null && value == null) return true;
            if(_value != null && _value.equals(value)) return true;
        }
        return false;
    }

    private int indexOfKey(Object key) {
        for(int i = 0; i < size(); i++) {
            K _key = keys[i];
            if(_key == null && key == null) return i;
            if(_key != null && _key.equals(key)) return i;
        }
        return -1;
    }

    private void setIndex(int index, K key, V value) {
        keys[index] = key;
        values[index] = value;
    }

    @Override
    public V put(K key, V value) {
        if(containsKey(key)) {
            int index = indexOfKey(key);
            V oldValue = values[index];

            keys[index] = key;
            values[index] = value;

            return oldValue;
        }

        K[] oldKeys = keys;
        V[] oldValues = values;

        setArrays(size() + 1);

        if(oldKeys != null) {
            System.arraycopy(oldKeys, 0, keys, 0, oldKeys.length);
            System.arraycopy(oldValues, 0, values, 0, oldValues.length);
        }

        setIndex(keys.length - 1, key, value);

        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        int length = 0;
        for(K key : m.keySet()) {
            if(!containsKey(key)) length++;
        }

        int index = values.length;

        if(length > 0) {
            final K[] oldKeys = keys;
            final V[] oldValues = values;

            setArrays(size() + length);

            if(oldKeys != null) {
                System.arraycopy(oldKeys, 0, keys, 0, oldKeys.length);
                System.arraycopy(oldValues, 0, values, 0, oldValues.length);
            }
        }

        for(K key : m.keySet()) {
            if(containsKey(key)) {
                int i = indexOfKey(key);
                setIndex(i, key, m.get(key));
            } else {
                setIndex(index, key, m.get(key));
                index++;
            }
        }
    }
}
