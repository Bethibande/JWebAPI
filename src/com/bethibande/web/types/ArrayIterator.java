package com.bethibande.web.types;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class ArrayIterator<T> implements Iterator<T> {

    public static <T> ArrayIterator<T> of(T... type) {
        return new ArrayIterator<>(type);
    }

    private T[] array;
    private int index = 0;

    public ArrayIterator(@NotNull T[] array) {
        this.array = array;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove is not supported!");
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public T next() {
        return array[index++];
    }
}
