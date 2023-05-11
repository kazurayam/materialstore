package com.kazurayam.materialstore.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StoreWriteParameterTest {

    @Test
    public void test_flowControl_default() {
        StoreWriteParameter p = new StoreWriteParameter.Builder().build();
        assertEquals(Jobber.DuplicationHandling.TERMINATE,
                p.getFlowControl());
    }

    @Test
    public void test_flowControl_specific() {
        StoreWriteParameter p =
                new StoreWriteParameter.Builder()
                        .flowControl(Jobber.DuplicationHandling.CONTINUE)
                        .build();
        assertEquals(Jobber.DuplicationHandling.CONTINUE,
                p.getFlowControl());
    }

    @Test
    public void test_jpegCompressionQuality_default() {
        StoreWriteParameter p = new StoreWriteParameter.Builder().build();
        assertEquals(0.9f, p.getJpegCompressionQuality());
    }

    @Test
    public void test_jpegCompressionQuality_specific() {
        Float v = 0.7f;
        StoreWriteParameter p =
                new StoreWriteParameter.Builder()
                        .jpegCompressionQuality(v)
                        .build();
        assertEquals(v, p.getJpegCompressionQuality());
    }

}
