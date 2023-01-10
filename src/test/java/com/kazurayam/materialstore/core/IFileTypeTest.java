package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.core.FileTypeDiffability;
import com.kazurayam.materialstore.core.IFileType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IFileTypeTest {

    @Test
    public void test_CustomFileType() {
        IFileType ft = new CustomFileType("foo", FileTypeDiffability.AS_TEXT);
        assertEquals("foo", ft.getExtension());
        assertEquals(FileTypeDiffability.AS_TEXT, ft.getDiffability());
    }

    private class CustomFileType implements IFileType {
        private String extension;
        private FileTypeDiffability diffability;
        CustomFileType(String extension, FileTypeDiffability diffability) {
            this.extension = extension;
            this.diffability = diffability;
        }
        @Override
        public String getExtension() {
            return this.extension;
        }
        @Override
        public FileTypeDiffability getDiffability() {
            return this.diffability;
        }
        @Override
        public List<String> getMimeTypes() {
            return Collections.emptyList();
        }
    }
}
