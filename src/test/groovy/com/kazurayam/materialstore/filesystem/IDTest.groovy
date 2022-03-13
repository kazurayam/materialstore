package com.kazurayam.materialstore.filesystem

import com.google.gson.Gson
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class IDTest {

    private static Gson gson = new Gson()

    @Test
    void test_toJson() {
        ID id = new ID("1234567890123456789012345678901234567890")
        String jsonified = id.toJson()
        assertEquals(id.toString(), jsonified)
    }
}
