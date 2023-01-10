package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.core.ID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IDTest {
    @Test
    public void test_toJson() {
        ID id = new ID("1234567890123456789012345678901234567890");
        String json = id.toJson();
        assertEquals("\"" + id.toString() + "\"", json);
    }

}
