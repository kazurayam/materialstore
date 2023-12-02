package com.kazurayam.materialstore.core.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetadataIdentificationTest {

    @Test
    public void test_smoke() {
        String identification = "{\"timestamp\":\"20221010-091203\", \"step\":\"01\"}";
        MetadataIdentification mi = new MetadataIdentification(identification);
        assertEquals(identification, mi.getIdentification());
        assertEquals(identification, mi.toString());
    }
}
