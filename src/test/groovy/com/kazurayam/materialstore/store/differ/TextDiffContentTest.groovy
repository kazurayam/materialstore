package com.kazurayam.materialstore.store.differ

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class TextDiffContentTest {

    @Test
    void test_smoke() {
        String content = "foo\nbar\naz\n"
        int inserted = 40
        int deleted = 0
        int changed = 9
        int equal = 92
        TextDiffContent tdc = new TextDiffContent.Builder(content)
                .inserted(inserted)
                .deleted(deleted)
                .changed(changed)
                .equal(equal)
                .build()
        assertEquals(content, tdc.getContent())
        assertEquals(inserted, tdc.getInserted())
        assertEquals(deleted, tdc.getDeleted())
        assertEquals(changed, tdc.getChanged())
        assertEquals(equal, tdc.getEqual())
        //
        assertEquals(inserted + deleted + changed + equal, tdc.getTotal())
        assertEquals(34.76d, tdc.getDiffRatio())
    }
}
