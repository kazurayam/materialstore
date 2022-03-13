package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataImpl
import groovy.xml.MarkupBuilder
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class Metadata implements Comparable {

    public static final Metadata NULL_OBJECT = new Builder().build()

    public static final String KEY_URL_PROTOCOL = "URL.protocol"
    public static final String KEY_URL_PORT = "URL.port"
    public static final String KEY_URL_HOST = "URL.host"
    public static final String KEY_URL_PATH = "URL.path"
    public static final String KEY_URL_QUERY = "URL.query"
    public static final String KEY_URL_FRAGMENT = "URL.fragment"

    abstract boolean containsKey(String key)
    abstract String get(String key)
    abstract boolean isEmpty()
    abstract Set<String> keySet()
    abstract int size()

    abstract String toURLAsString()

    abstract URL toURL()

    abstract void toSpanSequence(MarkupBuilder mb, QueryOnMetadata query)

    abstract void toSpanSequence(MarkupBuilder mb,
                                 QueryOnMetadata leftQuery,
                                 QueryOnMetadata rightQuery,
                                 IgnoreMetadataKeys ignoreMetadataKeys,
                                 IdentifyMetadataValues identifyMetadataValues)

    //------------------Comparable-------------------------------------
    abstract int compareTo(Object obj)

    // ---------------- factory method ---------------------
    static Builder builder() {
        return new Builder()
    }

    /**
     * with deep copy
     * @param source
     * @return
     */
    static Builder builder(Metadata source) {
        return new Builder(source)
    }

    static Builder builder(URL url) {
        return new Builder(url)
    }

    static Builder builder(Map map) {
        return new Builder(map)
    }


    // ----------------------- helper ----------------------------------
    /**
     *
     * @param queryString "q=katalon&dfe=piiipfe&cxw=fcfw"
     * @return List<org.apache.http.NameValuePair>
     */
    static List<NameValuePair> parseURLQuery(String queryString, Charset charset = StandardCharsets.UTF_8) {
        Objects.requireNonNull(queryString)
        return URLEncodedUtils.parse(queryString, charset)
    }

    /**
     *
     */
    public static class Builder {
        Map<String, String> metadata
        Builder() {
            metadata = new HashMap<String, String>()
        }
        Builder(Metadata source) {
            metadata = new HashMap<String, String>()
            for (String key : source.keySet()) {
                metadata.put(key, source.get(key))
            }
        }
        Builder(Map map) {
            Objects.requireNonNull(map)
            metadata = new HashMap(map)
        }
        Builder(URL url) {
            this()
            Objects.requireNonNull(url)
            metadata.put(KEY_URL_PROTOCOL, url.getProtocol())
            if (url.getProtocol().startsWith("http")) {
                if (url.getPort() < 0) {
                    metadata.put(KEY_URL_PORT, '80')
                } else {
                    metadata.put(KEY_URL_PORT, Integer.valueOf(url.getPort()).toString())
                }
            }
            metadata.put(KEY_URL_HOST, url.getHost())
            if (url.getPath() != null) {
                metadata.put(KEY_URL_PATH, url.getPath())
            }
            if (url.getQuery() != null) {
                metadata.put(KEY_URL_QUERY, url.getQuery())
            }
            int posHash = url.toString().indexOf("#")
            if (posHash >= 0) {
                metadata.put(KEY_URL_FRAGMENT, url.toString().substring(posHash + 1))
            }
        }
        Builder put(String key, String value) {
            Objects.requireNonNull(key)
            metadata.put(key, value)
            return this
        }
        Builder putAll(Map<String, String> m) {
            Objects.requireNonNull(m)
            metadata.putAll(m)
            return this
        }
        Metadata build() {
            return new MetadataImpl(metadata)
        }
    }

}