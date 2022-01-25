package com.kazurayam.materialstore

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class FileTypeTest {

    @Test
    void test_PNG() {
        assertEquals("png", FileType.PNG.getExtension())
        assertEquals(Diffability.AS_IMAGE, FileType.PNG.getDiffability())
        assertTrue(FileType.PNG.getMimeTypes().contains("image/png"))
    }

    @Test
    void test_CSS() {
        FileType ft = FileType.ofMimeType('text/css')
        assertEquals(FileType.CSS, ft)
        assertEquals(Diffability.AS_TEXT, ft.getDiffability())
    }

    @Test
    void test_JS() {
        FileType ft = FileType.ofMimeType('application/javascript')
        assertEquals(FileType.JS, ft)
        assertEquals(Diffability.AS_TEXT, ft.getDiffability())
    }

    @Test
    void test_HTML() {
        FileType ft = FileType.ofMimeType('text/html')
        assertEquals(FileType.HTML, ft)
        assertEquals(Diffability.AS_TEXT, ft.getDiffability())
    }

    @Test
    void test_WOFF2() {
        FileType ft = FileType.ofMimeType('font/woff2')
        assertEquals(FileType.WOFF2, ft)
        assertEquals(Diffability.UNABLE, ft.getDiffability())
    }


}
