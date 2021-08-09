package com.kazurayam.materialstore

interface Metadata extends MapLike, Comparable {

    public static final Metadata NULL_OBJECT = new MetadataImpl.Builder().build()

    boolean match(MetadataPattern metadataPattern)

    URL toURL()

    String KEY_URL_PROTOCOL = "URL.protocol"
    String KEY_URL_HOST = "URL.host"
    String KEY_URL_PATH = "URL.path"
    String KEY_URL_QUERY = "URL.query"
    String KEY_URL_FRAGMENT = "URL.fragment"

}