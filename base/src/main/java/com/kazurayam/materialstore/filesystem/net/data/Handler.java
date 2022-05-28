package com.kazurayam.materialstore.filesystem.net.data;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This class is used by com.kazurayam.materialstore.filesystem.metadata.MetadataImpl class
 * to support "data:" URL
 *
 * @author kazurayam
 */
public final class Handler extends URLStreamHandler {

    /**
     * we need to implment openConnection() method here in order to make this Handler class instanciate-able.
     *
     * But we will not use URL.openConnection() for data: URL in Katalon Studio.
     * We only use URL.toString() method for data: URL.
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return null;
    }

}