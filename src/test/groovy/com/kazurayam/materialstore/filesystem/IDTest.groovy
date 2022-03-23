package com.kazurayam.materialstore.filesystem


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class IDTest {

    @Test
    void test_toJson() {
        ID id = new ID("1234567890123456789012345678901234567890")
        String json = id.toJson()
        assertEquals("\"" + id.toString() + "\"", json)
    }
}
