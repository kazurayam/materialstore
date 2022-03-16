package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute
import com.kazurayam.materialstore.filesystem.metadata.MetadataImpl
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class Metadata implements Comparable, Jsonifiable, TemplateReady {

    public static final Metadata NULL_OBJECT = new Builder().build()

    public static final String KEY_URL_PROTOCOL = "URL.protocol"
    public static final String KEY_URL_PORT = "URL.port"
    public static final String KEY_URL_HOST = "URL.host"
    public static final String KEY_URL_PATH = "URL.path"
    public static final String KEY_URL_QUERY = "URL.query"
    public static final String KEY_URL_FRAGMENT = "URL.fragment"

    abstract void annotate(QueryOnMetadata query)
    abstract void annotate(QueryOnMetadata leftQuery,
                           QueryOnMetadata rightQuery,
                           IgnoreMetadataKeys ignoreMetadataKeys,
                           IdentifyMetadataValues identifyMetadataValues)
    abstract boolean canBeIdentified(String key, IdentifyMetadataValues identifyMetadataValues)
    abstract boolean canBePaired(QueryOnMetadata left, QueryOnMetadata right, String key)
    abstract boolean containsKey(String key)
    abstract String get(String key)
    abstract MetadataAttribute getMetadataAttribute(String key)
    abstract boolean isEmpty()
    abstract Set<String> keySet()
    abstract boolean matchesByAster(QueryOnMetadata query, String key)
    abstract boolean matchesIndividually(QueryOnMetadata query, String key)
    abstract int size()
    abstract String toURLAsString()
    abstract URL toURL()

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
        Map<String, MetadataAttribute> attributes
        Builder() {
            attributes = new HashMap<String, MetadataAttribute>()
        }
        Builder(Metadata source) {
            this()
            for (String key : source.keySet()) {
                attributes.put(key, source.getMetadataAttribute(key))
            }
        }
        Builder(Map<String, String> map) {
            this()
            for (String key : map.keySet()) {
                MetadataAttribute attribute = new MetadataAttribute(key, map.get(key))
                attributes.put(key, attribute)
            }
        }
        Builder(URL url) {
            this()
            Objects.requireNonNull(url)
            attributes.put(KEY_URL_PROTOCOL,
                    new MetadataAttribute(KEY_URL_PROTOCOL,
                            url.getProtocol()))
            if (url.getProtocol().startsWith("http")) {
                if (url.getPort() < 0) {
                    attributes.put(KEY_URL_PORT,
                            new MetadataAttribute(KEY_URL_PORT, '80'))
                } else {
                    attributes.put(KEY_URL_PORT,
                            new MetadataAttribute(KEY_URL_PORT,
                                    Integer.valueOf(url.getPort()).toString()))
                }
            }
            attributes.put(KEY_URL_HOST,
                    new MetadataAttribute(KEY_URL_HOST, url.getHost()))
            if (url.getPath() != null) {
                attributes.put(KEY_URL_PATH,
                        new MetadataAttribute(KEY_URL_PATH, url.getPath()))
            }
            if (url.getQuery() != null) {
                attributes.put(KEY_URL_QUERY,
                        new MetadataAttribute(KEY_URL_QUERY, url.getQuery()))
            }
            int posHash = url.toString().indexOf("#")
            if (posHash >= 0) {
                attributes.put(KEY_URL_FRAGMENT,
                        new MetadataAttribute(KEY_URL_FRAGMENT,
                                url.toString().substring(posHash + 1)))
            }
        }
        Builder put(String key, String value) {
            Objects.requireNonNull(key)
            attributes.put(key, new MetadataAttribute(key, value))
            return this
        }
        Builder putAll(Map<String, String> m) {
            Objects.requireNonNull(m)
            for (String key : m.keySet()) {
                attributes.put(key, new MetadataAttribute(key, m.get(key)))
            }
            return this
        }
        Metadata build() {
            return new MetadataImpl(attributes)
        }
    }

}