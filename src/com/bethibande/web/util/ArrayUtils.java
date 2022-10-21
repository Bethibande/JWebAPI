package com.bethibande.web.util;

import java.lang.reflect.Array;

public class ArrayUtils {

    /**
     * Copies src array to dest array whilst discarding all items within the selected range.<br>
     * <br>
     * rangeCopy(src, dest, 1, 2); <br>
     * src (0 | 1 | 2 | 3) -> dest (0 | 3) <br>
     * <br>
     * Deletes 2 items starting at index 1
     */
    public static void rangeCopy(Object[] src, Object[] dest, int start, int length) {
        if(start > 0) {
            System.arraycopy(src, 0, dest, 0, start);
        }
        if(start + length != src.length) {
            System.arraycopy(src, start + length, dest, start, src.length - (start + length));
        }
    }

    public static <T> T[] createArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

}
