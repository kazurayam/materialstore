package com.kazurayam.materialstore

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.NameValuePair

/**
 * Metadata is an immutable object.
 */
class MetadataImpl implements Metadata {


    private final Map<String, String> metadata


    private MetadataImpl(Builder builder) {
        this.metadata = builder.metadata
    }

    // ------------ implements MapLike -------------------

    @Override
    boolean containsKey(String key) {
        return metadata.containsKey((String)key)
    }

    @Override
    String get(String key) {
        return metadata.get(key)
    }

    @Override
    boolean isEmpty() {
        return metadata.isEmpty()
    }

    @Override
    Set<String> keySet() {
        return metadata.keySet()
    }

    @Override
    int size()  {
        metadata.size()
    }

    @Override
    URL toURL() {
        if (metadata.containsKey(Metadata.KEY_URL_HOST)) {
            StringBuilder sb = new StringBuilder()
            sb.append(metadata.get(Metadata.KEY_URL_PROTOCOL))
            sb.append("://")
            sb.append(metadata.get(Metadata.KEY_URL_HOST))
            sb.append(metadata.get(Metadata.KEY_URL_PATH))
            if (metadata.containsKey(Metadata.KEY_URL_QUERY)) {
                sb.append("?")
                sb.append(metadata.get(Metadata.KEY_URL_QUERY))
            }
            return new URL(sb.toString())
        } else {
            return null
        }
    }

    // -------------- Metadata -------------------
    /**
     *
     * @param metadataPattern
     * @return
     */
    boolean match(MetadataPattern metadataPattern) {
        boolean result = true
        metadataPattern.keySet().each { key ->
            if (this.keySet().contains(key)) {
                String pattern = metadataPattern.get(key)
                if (pattern == "*") {
                    ;
                } else if (pattern == this.get(key)) {
                    ;
                } else {
                    result = false
                    return
                }
            } else {
                result = false
                return
            }
        }
        return result
    }

    // ------- overriding java.lang.Object -------

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MetadataImpl) {
            return false
        }
        MetadataImpl other = (MetadataImpl)obj
        if (other.size() != other.metadata.size()) {
            return false
        }
        //
        Set otherKeySet = other.keySet()
        if (this.keySet() != otherKeySet) {
            return false
        }
        //
        boolean result = true
        this.keySet().each { key ->
            if (this.get(key) != other.get(key)) {
                result = false
                return
            }
        }
        return result
    }


    @Override
    int hashCode() {
        int hash = 7
        this.keySet().each { key ->
            hash = 31 * hash + key.hashCode()
            hash = 31 * hash + this.get(key).hashCode()
        }
        return hash
    }

    @Override
    String toString() {
        //return new JsonOutput().toJson(metadata)
        StringBuilder sb = new StringBuilder()
        int entryCount = 0
        sb.append("{")
        assert metadata != null, "metadata_ is null before iterating over keys"
        //println "keys: ${metadata_.keySet()}"
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Map<String,String> copy = new HashMap<String,String>(metadata)
        // sort by the key
        Collections.sort(keys)
        keys.each { key ->
            if (entryCount > 0) {
                sb.append(", ")    // comma followed by a white space
            }
            sb.append('"')
            assert copy != null, "metadata_ is null for key=${key}"
            sb.append(JsonUtil.escapeAsJsonString(key))
            sb.append('"')
            sb.append(':')
            sb.append('"')
            sb.append(JsonUtil.escapeAsJsonString(copy.get(key)))
            sb.append('"')
            entryCount += 1
        }
        sb.append("}")
        return sb.toString()
    }

    // ------------ comparable ----------------
    @Override
    int compareTo(Object obj) {
        if (! obj instanceof MetadataImpl) {
            throw new IllegalArgumentException("obj is not instance of Metadata")
        }
        MetadataImpl other = (MetadataImpl)(obj)
        return this.toString() <=> other.toString()
    }


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
    static class Builder {
        Map<String, String> metadata
        Builder() {
            metadata = new HashMap<String, String>()
        }
        Builder(Map map) {
            Objects.requireNonNull(map)
            metadata = new HashMap(map)
        }
        Builder(URL url) {
            this()
            Objects.requireNonNull(url)
            metadata.put(Metadata.KEY_URL_PROTOCOL, url.getProtocol())
            metadata.put(Metadata.KEY_URL_HOST, url.getHost())
            metadata.put(Metadata.KEY_URL_PATH, url.getPath())
            String query = url.getQuery()
            if (query != null) {
                metadata.put(Metadata.KEY_URL_QUERY, query)
            }
        }
        Builder put(String key, String value) {
            Objects.requireNonNull(key)
            metadata.put(key, value)
            return this
        }
        Metadata build() {
            return new MetadataImpl(this)
        }
    }

}
