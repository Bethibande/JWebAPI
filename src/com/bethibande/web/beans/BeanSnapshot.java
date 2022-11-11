package com.bethibande.web.beans;

import java.lang.reflect.Field;
import java.util.HashMap;

public class BeanSnapshot {

    private final Class<?> type;
    private final HashMap<Field, Object> state;

    public BeanSnapshot(Class<?> type, HashMap<Field, Object> state) {
        this.type = type;
        this.state = state;
    }

    public Class<?> getType() {
        return type;
    }

    public HashMap<Field, Object> getState() {
        return state;
    }
}
