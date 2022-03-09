package com.kazurayam.materialstore.net.data

class DataURLEnabler {

    static void enableDataURL() {
        // as described in the Javadoc of java.net.URL class at
        // https://docs.oracle.com/javase/7/docs/api/java/net/URL.html
        String propName = 'java.protocol.handler.pkgs'
        String prop = System.getProperty(propName)
        String previousProp = (prop == null) ? "" : prop + "|"
        String pkg = 'com.kazurayam.materialstore.net'
        if ( ! previousProp.contains(pkg) ) {
            System.setProperty(propName, previousProp + pkg)
        }
    }

}
