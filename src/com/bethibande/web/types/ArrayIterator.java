package com.bethibande.web.types;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {

    @SafeVarargs
    public static <T> ArrayIterator<T> of(T... array) {
        return new ArrayIterator<>(array);
    }

    private final T[] array;
    private int index = 0;

    public ArrayIterator(T[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public T next() {
        return array[index++];
    }

    @Override
    public void remove() {
        throw new RuntimeException("remove action is not supported.");
    }
}
