package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

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

    @Test
    void test_ofMimeType_CSS() {
        FileType ft = FileType.ofMimeType('text/css')
        assertEquals(FileType.CSS, ft)
    }

    @Test
    void test_ofMimeType_JS() {
        FileType ft = FileType.ofMimeType('application/javascript')
        assertEquals(FileType.JS, ft)
    }

    @Test
    void test_ofMimeType_HTML() {
        FileType ft = FileType.ofMimeType('text/html')
        assertEquals(FileType.HTML, ft)
    }

    @Test
    void test_ofMimeType_WOFF2() {
        FileType ft = FileType.ofMimeType('font/woff2')
        assertEquals(FileType.WOFF2, ft)
    }


}
