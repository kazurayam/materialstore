package com.kazurayam.materialstore.store


import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

class FileTypeTest {

    @Test
    void test_getExtension_png() {
        assertEquals("png", FileType.PNG.getExtension())
    }

    @Test
    void test_getMimeTypes_png() {
        List<String> mtypes = FileType.PNG.getMimeTypes()
        assertTrue(mtypes.contains("image/png"))
    }
}
