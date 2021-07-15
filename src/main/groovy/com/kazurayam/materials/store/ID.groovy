package com.kazurayam.materials.store

class ID implements Comparable {

    static final ID NULL_OBJECT = new ID("0" * 40)

    private String sha1_

    static boolean isValid(String sha1) {
        if (sha1.length() != 40) {
            throw new IllegalArgumentException("sha1(${sha1}) must be of length=40")
        }
        if (sha1.replaceAll("[0-9a-fA-F]", "").length() != 0) {
            throw new IllegalArgumentException("sha1(${sha1}) must be consits of only hex-decimal characters")
        }
        return true
    }

    ID(String sha1) {
        Objects.requireNonNull(sha1)
        isValid(sha1)
        this.sha1_ = sha1
    }

    String getSha1() {
        return sha1_
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof ID) {
            return false
        }
        ID other = (ID)obj
        return getSha1() == other.getSha1()
    }

    @Override
    int hashCode() {
        return getSha1().hashCode()
    }

    @Override
    String toString() {
        return getSha1()
    }

    @Override
    int compareTo(Object o) {
        if (! o instanceof ID) {
            throw new IllegalArgumentException("not an instance of ID")
        }
        ID other = (ID)o
        return getSha1() <=> other.getSha1()
    }
}
