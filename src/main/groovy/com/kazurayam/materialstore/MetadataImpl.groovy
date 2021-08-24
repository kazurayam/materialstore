package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 */
final class MetadataImpl extends Metadata {

    private final Map<String, String> metadata

    private MetadataImpl(Map<String, String> metadata) {
        this.metadata = metadata
    }

    /**
     * the static factory method
     * @param metadata
     * @return
     */
    static MetadataImpl from(Map<String, String> metadata) {
        return new MetadataImpl(metadata)
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


    // -------------- overrides methods of Metadata -------------------
    /**
     *
     * @param metadataPattern
     * @return
     */
    @Override
    boolean match(MetadataPattern metadataPattern) throws MaterialstoreException {
        boolean result = true
        metadataPattern.keySet().each { key ->
            if (key == "*" || this.keySet().contains(key)) {
                if (metadataPattern.get(key) instanceof Pattern) {
                    Pattern pattern = (Pattern)metadataPattern.get(key)
                    Matcher matcher = pattern.matcher(this.get(key))
                    if (matcher.find()) {
                        ;
                    } else {
                        result = false
                        return
                    }
                } else {
                    String ptnString = (String)metadataPattern.get(key)
                    if (this.get(key) == ptnString) {
                        ;
                    } else {
                        result = false
                        return
                    }
                }
            } else {
                result = false
                return
            }
        }
        return result
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

    @Override
    void toSpanSequence(MarkupBuilder mb, MetadataPattern metadataPattern) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(metadataPattern)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach {key ->
            if (count > 0) {
                mb.span(", ")
            }
            mb.span("\"${JsonUtil.escapeAsJsonString(key)}\":")
            if (metadataPattern.containsKey(key)) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            }
            count += 1
        }
        mb.span("}")
    }

    @Override
    void toSpanSequence(MarkupBuilder mb,
                        MetadataPattern leftMetadataPattern,
                        MetadataPattern rightMetadataPattern,
                        IgnoringMetadataKeys ignoringMetadataKeys) {
        Objects.requireNonNull(mb)
        Objects.requireNonNull(leftMetadataPattern)
        Objects.requireNonNull(rightMetadataPattern)
        Objects.requireNonNull(ignoringMetadataKeys)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach {key ->
            if (count > 0) {
                mb.span(", ")
            }
            if (ignoringMetadataKeys.contains(key)) {
                mb.span(class: "ignoring-key", "\"${JsonUtil.escapeAsJsonString(key)}\":")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(key)}\":")
            }
            if (leftMetadataPattern.containsKey(key)) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else if (rightMetadataPattern.containsKey(key)) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            }
            count += 1
        }
        mb.span("}")
    }

    // ------- overriding java.lang.Object -------
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



    // ------------ comparable ----------------
    @Override
    int compareTo(Object obj) {
        if (! obj instanceof MetadataImpl) {
            throw new IllegalArgumentException("obj is not instance of Metadata")
        }
        MetadataImpl other = (MetadataImpl)(obj)
        return this.toString() <=> other.toString()
    }




}
