package com.alteredworlds.taptap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private static final String LOG_TAG = ExampleUnitTest.class.getSimpleName();
    
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testArrays() {
        int[][][] array1 = new int[1][3][5];
        int[][][][] array2 = new int[2][1][0][1];

        assertEquals(1, array1.length);
        assertEquals(3, array1[0].length);
        assertEquals(5, array1[0][1].length);

        assertEquals(2, array2.length);
        assertEquals(1, array2[1].length);
        assertEquals(0, array2[1][0].length);

        String[][][] str = new String[2][][];

        assertEquals(2, str.length);
        assertEquals(null, str[1]);
    }
}