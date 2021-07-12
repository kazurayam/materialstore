package com.kazurayam.taod

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class JobNameTest {

    @Test
    void test_validate() {
        assertThrows(IllegalArgumentException.class, { ->
            JobName.validate("/")
        })
    }

    @Test
    void test_toString() {
        assertEquals("foo", new JobName("foo").toString())
    }
}
