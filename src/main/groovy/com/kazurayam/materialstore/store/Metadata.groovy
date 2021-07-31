package com.kazurayam.materialstore.store

import groovy.json.JsonOutput
import com.kazurayam.materialstore.JsonUtil

/**
 * Metadata is an immutable object.
 */
class Metadata implements Comparable {

    static final Metadata NULL_OBJECT = new Metadata([:])

    private final Map<String, String> metadata_ = new TreeMap<String, String>()  // keys are sorted

    Metadata(Map<String, String> source) {
        Objects.requireNonNull(source)
        metadata_.putAll(source)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Metadata) {
            throw new IllegalArgumentException("obj is not instance of Metadata")
        }
        Metadata other = (Metadata)(obj)
        return this.toString() <=> other.toString()
    }

    boolean containsKey(Object key) {
        return metadata_.containsKey((String)key)
    }

    boolean containsValue(Object value) {
        return metadata_.containsValue((String)value)
    }

    Set<Map.Entry<String, String>> entrySet() {
        return metadata_.entrySet()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Metadata) {
            return false
        }
        Metadata other = (Metadata)obj
        if (other.size() != other.metadata_.size()) {
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

    String get(String key) {
        return metadata_.get(key)
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

    boolean isEmpty() {
        return metadata_.isEmpty()
    }

    /**
     * will return sorted set of keys, as the metadata_ is an instance of TreeSet
     * @return
     */
    Set<String> keySet() {
        return metadata_.keySet()
    }

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

    int size()  {
        metadata_.size()
    }

    @Override
    String toString() {
        //return new JsonOutput().toJson(metadata_)
        StringBuilder sb = new StringBuilder()
        int entryCount = 0
        sb.append("{")
        assert metadata_ != null, "metadata_ is null before iterating over keys"
        //println "keys: ${metadata_.keySet()}"
        List<String> keys = new ArrayList<String>(metadata_.keySet())
        Map<String,String> copy = new HashMap<String,String>(metadata_)
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

    Collection<String> values() {
        return metadata_.values()
    }

}
