package com.kazurayam.materials.store

import groovy.json.JsonOutput

class Metadata implements Comparable {

    static final Metadata NULL_OBJECT = new Metadata()

    private final List<String> metadata_ = new ArrayList<String>()

    Metadata(String... metadata) {
        for (String entry in metadata) {
            metadata_.add(entry)
        }
    }

    Metadata(List<String> metadata) {
        this.metadata_ = new ArrayList<String>()
        for (String entry in metadata) {
            metadata_.add(entry)
        }
    }

    void add(String entry) {
        metadata_.add(entry)
    }

    void addAll(List<String> entries) {
        for (String entry in entries) {
            this.add(entry)
        }
    }

    int size()  {
        metadata_.size()
    }

    String entry(int i) {
        return metadata_.get(i)
    }

    Iterator iterator() {
        return metadata_.iterator()
    }

    boolean match(MetadataPattern metadataPattern) {
        boolean result = true
        metadataPattern.eachWithIndex { pattern, index ->
            if (index < metadata_.size()) {
                if (pattern == "*") {
                    ;
                } else if (pattern == metadata_.get(index)) {
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

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Metadata) {
            throw new IllegalArgumentException("obj is not an instance of Metadata")
        }
        Metadata other = (Metadata)obj
        int i = 0
        for ( ; i < this.size(); i++) {
            if (other.size() <= i) {
                return 1
            }
            int result = this.entry(i) <=> other.entry(i)
            if (result != 0) {
                return result
            }
        }
        if (i < other.size()) {
            return -1
        }
        return 0
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
        for (int i = 0; i < other.size(); i++) {
            if (other.get(i) != this.get(i)) {
                return false
            }
        }
        return true
    }

    @Override
    int hashCode() {
        int hash = 7;
        for (int i = 0; i < this.size(); i++) {
            hash = 31 * hash + this.get(i).hashCode()
        }
        return hash;
    }

    @Override
    String toString() {
        return new JsonOutput().toJson(metadata_)
    }
}
