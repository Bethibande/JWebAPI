package com.bethibande.web.utils;

import com.bethibande.web.util.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayUtilsTest {

    @Test
    public void testRangeCopy() {
        String[] array1 = new String[] {"a", "b", "c", "d"};
        String[] array2 = new String[3];

        ArrayUtils.rangeCopy(array1, array2, 1, 1);

        assertEquals(Arrays.toString(new String[] {"a", "c", "d"}), Arrays.toString(array2));

        String[] array3 = new String[2];

        ArrayUtils.rangeCopy(array1, array3, 1, 2);

        assertEquals(Arrays.toString(new String[] {"a", "d"}), Arrays.toString(array3));
    }

}
