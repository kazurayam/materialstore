package com.kazurayam.materialstore.filesystem.metadata

import groovy.xml.MarkupBuilder

abstract class IgnoreMetadataKeys {

    public static final IgnoreMetadataKeys NULL_OBJECT = new Builder().build()

    abstract int size()

    abstract Set<String> keySet()

    abstract boolean contains(String key)

    abstract Iterator<String> iterator()

    abstract void toSpanSequence(MarkupBuilder mb)


    /**
     *
     */
    static class Builder {
        Set<String> keySet
        Builder() {
            this.keySet = new HashSet<String>()
        }
        Builder ignoreKey(String key) {
            this.keySet.add(key)
            return this
        }
        Builder ignoreKeys(String... keys) {
            keys.each {
                this.keySet.add(it)
            }
            return this
        }
        IgnoreMetadataKeys build() {
            return new IgnoreMetadataKeysImpl(keySet)
        }
    }
}
