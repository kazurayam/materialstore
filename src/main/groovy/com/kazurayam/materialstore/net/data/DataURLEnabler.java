package com.kazurayam.materialstore.net.data;

public class DataURLEnabler {

    public static final String PROPNAME = "java.protocol.handler.pkgs";

    public static void enableDataURL() {
        // as described in the Javadoc of java.net.URL class at
        // https://docs.oracle.com/javase/7/docs/api/java/net/URL.html
        String prop = System.getProperty(PROPNAME);
        String previousProp = (prop == null) ? "" : prop + "|";
        String pkg = "com.kazurayam.materialstore.net";
        if (!previousProp.contains(pkg)) {
            System.setProperty(PROPNAME, previousProp + pkg);
        }
    }

}