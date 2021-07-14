package com.kazurayam.materials.store

import com.kazurayam.materials.store.JobName
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

class JobNameTest {

    @Test
    void test_isValid() {
        assertFalse(JobName.isValid("/"))
    }

    @Test
    void test_toString() {
        assertEquals("foo", new JobName("foo").toString())
    }
}
