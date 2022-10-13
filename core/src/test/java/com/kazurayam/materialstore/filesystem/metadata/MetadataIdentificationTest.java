package com.kazurayam.materialstore.filesystem.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetadataIdentificationTest {

    @Test
    public void test_smoke() {
        String representation = "{\"timestamp\":\"20221010-091203\", \"step\":\"01\"}";
        MetadataIdentification mi = new MetadataIdentification(representation);
        assertEquals(representation, mi.getRepresentation());
        assertEquals(representation, mi.toString());
    }
}
