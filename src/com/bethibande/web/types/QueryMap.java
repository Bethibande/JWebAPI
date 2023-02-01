package com.bethibande.web.types;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;

public class QueryMap {

    private final HashMap<String, String> query = new HashMap<>();

    private final String queryString;
    private final Charset charset;

    public QueryMap(final String queryString, final Charset charset) {
        this.queryString = queryString;
        this.charset = charset;

        parseMap();
    }

    private void parseMap() {
        if(queryString == null || queryString.isEmpty()) return;

        for(String str : queryString.split("&")) {
            int index = str.indexOf('=');
            final String key = index == -1 ? str: str.substring(0, index);
            String value = index == -1 ? null : str.substring(index+1);
            value = URLDecoder.decode(value, charset);

            query.put(key, value);
        }
    }

    public boolean hasKey(String key) {
        return query.containsKey(key);
    }

    /**
     * Returns the query value as a boolean using Boolean.parseBoolean, if there is no value
     * it will return {@link #hasKey(String)}
     */
    public boolean getAsBoolean(String key) {
        String value = getAsString(key);
        if(value == null) return hasKey(key);
        return Boolean.parseBoolean(value);
    }

    public Byte getAsByte(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Byte.parseByte(value);
    }

    public Short getAsShort(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Short.parseShort(value);
    }

    public Integer getAsInt(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Integer.parseInt(value);
    }

    public Long getAsLong(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Long.parseLong(value);
    }

    public Float getAsFloat(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Float.parseFloat(value);
    }

    public Double getAsDouble(String key) {
        String value = getAsString(key);
        if(value == null) return null;
        return Double.parseDouble(value);
    }

    public String getAsString(String key) {
        return query.get(key);
    }

}
