package com.kazurayam.materialstore.filesystem

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class JobNameTest {

    /**
     *  isValid(String) throws an IllegalArgumentException when the argument String contains
     *  a character listed in the com.kazurayam.materialstore.FileName.PROHIBITED_CHARACTERS.
     *  For example, '/' is prohibited.
     */
    @Test
    void test_isValid() {
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, { ->
            JobName.isValid("/")
        })
        assertTrue(e.getMessage().contains("/") && e.getMessage().contains("prohibited"))
    }

    @Test
    void test_toString() {
        assertEquals("foo", new JobName("foo").toString())
    }
}
