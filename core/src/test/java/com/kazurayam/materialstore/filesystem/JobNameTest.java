package com.kazurayam.materialstore.filesystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JobNameTest {
    /**
     * isValid(String) throws an IllegalArgumentException when the argument String contains
     * a character listed in the com.kazurayam.materialstore.FileName.PROHIBITED_CHARACTERS.
     * For example, '/' is prohibited.
     */
    @Test
    public void test_isValid() {
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JobName.isValid("/");
        });
        Assertions.assertTrue(e.getMessage().contains("/") && e.getMessage().contains("prohibited"));
    }

    @Test
    public void test_toString() {
        Assertions.assertEquals("foo", new JobName("foo").toString());
    }

}
