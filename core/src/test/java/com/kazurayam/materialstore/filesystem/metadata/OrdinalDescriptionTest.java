package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrdinalDescriptionTest {

    @Test
    public void test_smoke() {
        String representation = "{\"timestamp\":\"20221010-091203\", \"step\":\"01\"}";
        OrdinalDescription od = new OrdinalDescription(representation);
        assertEquals(representation, od.getRepresentation());
        assertEquals(representation, od.toString());
    }
}
