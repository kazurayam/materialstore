package com.kazurayam.materialstore.core.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory that instantiates a {@link java.nio.file.FileSystem} to allocate the Store.
 */
public final class FileSystemFactory {
    private FileSystemFactory() {}

    private static final Logger logger = LoggerFactory.getLogger(FileSystemFactory.class);

    public static final String SYSTEM_PROPERTY_NAME = "s3fs.uri";

    /**
     *
     */
    static FileSystem newFileSystem() throws IOException {
        if (System.getProperty(SYSTEM_PROPERTY_NAME) != null) {
            try {
                URI uri = new URI(System.getProperty(SYSTEM_PROPERTY_NAME));
                reviewURI(uri);
                return FileSystems.newFileSystem(uri,
                            new HashMap<String, Object>(),
                            Thread.currentThread().getContextClassLoader());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            return FileSystems.getDefault();
        }
    }

    /**
     *
     */
    static FileSystem newFileSystem(URI uri) throws IOException {
        reviewURI(uri);
        return FileSystems.newFileSystem(uri,
                new HashMap<String, Object>(),
                Thread.currentThread().getContextClassLoader());
    }

    private static void reviewURI(URI uri) {
        Set<URI> s3endpoints = getAllS3EndpointURIs();
        logger.debug("creating a new FileSystem with URI " + uri.toString());
        if (s3endpoints.contains(uri)) {
            logger.debug("known S3 URI: " + uri.toString());
        } else {
            logger.warn("unknown URI: " + uri.toString());
        }
    }

    private static Set<URI> getAllS3EndpointURIs() {
        Set<URI> set = new HashSet<>();
        for (S3Endpoint ep : S3Endpoint.values()) {
            set.add(ep.getURI());
        }
        return set;
    }
}
