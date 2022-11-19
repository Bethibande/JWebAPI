package com.bethibande.web.beans;

import java.lang.reflect.Field;
import java.util.HashMap;

public record BeanSnapshot(Class<?> type, HashMap<Field, Object> state) {

}
