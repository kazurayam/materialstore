package com.kazurayam.materialstore.diff

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class DifferDriverFactoryTest {

    @Test
    void test_newDifferDriver() {
        DifferDriver dd = DifferDriverFactory.newDifferDriver()
        assertNotNull(dd)
        assertTrue(dd instanceof DifferDriverImpl )
    }
}
