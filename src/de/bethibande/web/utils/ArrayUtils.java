package de.bethibande.web.utils;

import java.util.Arrays;

public class ArrayUtils {

    public static byte[] trim(byte[] arr, int start, int end) {
        return Arrays.copyOfRange(arr, start, end);
    }

}
