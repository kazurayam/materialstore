package com.kazurayam.taod

import com.google.gson.Gson

class Metadata implements Comparable {

    List<String> metadata_ = new ArrayList<String>()

    Metadata(String... metadata) {
        for (String entry in metadata) {
            metadata_.add(entry)
        }
    }

    Metadata(List<String> metadata) {
        this.metadata_ = new ArrayList<String>()
    }

    void add(String entry) {
        metadata_.add(entry.trim())
    }

    void addAll(List<String> entries) {
        for (String entry in entries) {
            this.add(entry)
        }
    }

    int size() {
        return metadata_.size()
    }

    String get(int i) {
        return metadata_.get(i)
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
            int result = this.get(i) <=> other.get(i)
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
        Gson gson = new Gson()
        return gson.toJson(metadata_)
    }
}
