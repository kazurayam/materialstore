package com.kazurayam.materialstore.filesystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTypeUtilTest {

    @Test
    public void test_getByExtension() {
        FileType expected = FileType.PNG;
        FileType actual = FileTypeUtil.getByExtension("png");
        assertEquals(actual, expected);
    }

    @Test
    public void test_getFileTypesDiffableAsImage() {
        List<IFileType> fileTypes = FileTypeUtil.getFileTypesDiffableAsImage();
        assertTrue(fileTypes.contains(FileType.PNG));
    }

    @Test
    public void test_getFileTypesDiffableAsText() {
        List<IFileType> fileTypes = FileTypeUtil.getFileTypesDiffableAsText();
        assertTrue(fileTypes.contains(FileType.HTML));
    }

    @Test
    public void test_getFileTypesUnableToDiff() {
        List<IFileType> fileTypes = FileTypeUtil.getFileTypesUnableToDiff();
        assertTrue(fileTypes.contains(FileType.ZIP));
    }

    @Test
    public void test_ofMimeType() {
        FileType expected = FileType.HTML;
        FileType actual = FileTypeUtil.ofMimeType("text/html");
        assertEquals(expected, actual);
    }
}
