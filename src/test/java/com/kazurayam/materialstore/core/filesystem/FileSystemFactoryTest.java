package com.kazurayam.materialstore.core.filesystem;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemFactoryTest {

    Logger logger = LoggerFactory.getLogger(FileSystemFactoryTest.class);

    @Test
    public void test_newFileSystem_with_URI() throws IOException {
        URI uri = S3Endpoint.AP_NORTHEAST_1.getURI();
        FileSystem fs = FileSystemFactory.newFileSystem(uri);
        assertNotNull(fs);
        String bucketName = "com.kazurayam.filesystemfactorytest";
        String dirName = "test_newFileSystem_with_URI";
        Path p = fs.getPath("/" + bucketName, dirName);
        logger.info("[" + dirName + "] " + p.toString());
        assertEquals(uri.toString() + "/" + bucketName + "/" + dirName,
                p.toString());
    }

    @Test
    public void test_newFileSystem_with_SystemProperty() throws IOException {
        System.setProperty(FileSystemFactory.SYSTEM_PROPERTY_NAME,
                S3Endpoint.AP_NORTHEAST_3.getURI().toString());
        FileSystem fs = FileSystemFactory.newFileSystem();
        String bucketName = "com.kazurayam.filesystemfactorytest";
        String dirName = "test_newFileSystem_with_SystemProperty";
        Path p = fs.getPath("/" + bucketName, dirName);
        logger.info("[" + dirName + "] " + p.toString());
        logger.info("[" + dirName + "] " + p.getClass().toString());
        assertEquals(S3Endpoint.AP_NORTHEAST_3.getURI().toString() +
                        "/" + bucketName + "/" + dirName,
                p.toString());
    }

    @Test
    public void test_newFileSystem_default() throws IOException {
        // make sure the System properties is NOT defined
        if (System.getProperty(FileSystemFactory.SYSTEM_PROPERTY_NAME) != null) {
            System.getProperties().remove(FileSystemFactory.SYSTEM_PROPERTY_NAME);
        }
        FileSystem fs = FileSystemFactory.newFileSystem();
        String dirName = "test_newFileSystem_default";
        Path p = fs.getPath("com.kazurayam.filesystemfactorytest",
                dirName)
                .toAbsolutePath();
        logger.info("[" + dirName + "] " + p.toString());
        logger.info("[" + dirName + "] " + p.getClass().getName());
    }

    @Test
    public void test_Paths_get() {
        Path dotPath = Paths.get(".").toAbsolutePath();
        logger.info("[test_Paths_get] dotPath=" + dotPath.toString());
    }
}
