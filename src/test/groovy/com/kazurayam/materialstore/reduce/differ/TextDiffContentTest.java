package com.kazurayam.materialstore.reduce.differ;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextDiffContentTest {
    @Test
    public void test_smoke() {
        String content = "foo\nbar\naz\n";
        int inserted = 40;
        int deleted = 0;
        int changed = 9;
        int equal = 92;
        TextDiffContent tdc = new TextDiffContent.Builder(content).inserted(inserted).deleted(deleted).changed(changed).equal(equal).build();
        Assertions.assertEquals(content, tdc.getContent());
        Assertions.assertEquals(inserted, tdc.getInserted());
        Assertions.assertEquals(deleted, tdc.getDeleted());
        Assertions.assertEquals(changed, tdc.getChanged());
        Assertions.assertEquals(equal, tdc.getEqual());
        //
        Assertions.assertEquals(inserted + deleted + changed + equal, tdc.getTotal());
        Assertions.assertEquals(34.76d, tdc.getDiffRatio());
    }

}
