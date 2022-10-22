package com.bethibande.web.sessions;

import java.util.HashMap;

public class MetaData {

    private HashMap<Object, Object> metadata = new HashMap<>();

    public boolean hasMeta(Object key) {
        return metadata.containsKey(key);
    }

    public void remove(Object key) {
        metadata.remove(key);
    }

    public void set(Object key, Object data) {
        metadata.put(key, data);
    }

    public Object get(Object key) {
        return metadata.get(key);
    }

    public Boolean getBoolean(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Boolean b)) return null;
        return b;
    }

    public Byte getByte(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Byte b)) return null;
        return b;
    }

    public Short getShort(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Short s)) return null;
        return s;
    }

    public Integer getInteger(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Integer i)) return null;
        return i;
    }

    public Long getLong(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Long l)) return null;
        return l;
    }

    public Float getFloat(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Float f)) return null;
        return f;
    }

    public Double getDouble(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Double d)) return null;
        return d;
    }

    public String getString(Object key) {
        Object obj = get(key);
        if(!(obj instanceof String s)) return null;
        return s;
    }

    public Character getCharacter(Object key) {
        Object obj = get(key);
        if(!(obj instanceof Character c)) return null;
        return c;
    }

    public <T> T getAsType(Object key, Class<T> type) {
        Object obj = get(key);
        if(obj == null) return null;
        return type.cast(obj);
    }

}
