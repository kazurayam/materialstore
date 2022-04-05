package com.kazurayam.materialstore.facet.textgrid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyRangeTest {

    @Test
    public void test_smoke() {
        KeyRange kr = new KeyRange(0, 2);
        assertEquals(0, kr.getFrom());
        assertEquals(2, kr.getTo());
    }
}
