package com.kazurayam.materialstore.store

import groovy.json.JsonOutput

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
        return new JsonOutput().toJson(metadata_)
    }

    Collection<String> values() {
        return metadata_.values()
    }

}
