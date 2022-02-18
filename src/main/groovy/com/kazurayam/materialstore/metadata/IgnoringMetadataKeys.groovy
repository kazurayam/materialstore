package com.kazurayam.materialstore.metadata

import groovy.xml.MarkupBuilder

abstract class IgnoringMetadataKeys {

    public static final IgnoringMetadataKeys NULL_OBJECT = new Builder().build()

    abstract int size()

    abstract boolean contains(String key)

    abstract Iterator<String> iterator()

    abstract void toSpanSequence(MarkupBuilder mb)

    // -------------------- factory methods ------------
    static IgnoringMetadataKeys of(String... keys) {
        Builder builder = new Builder()
        keys.each {
            builder.ignoreKey(it)
        }
        return builder.build()
    }


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
        IgnoringMetadataKeys build() {
            return new IgnoringMetadataKeysImpl(keySet)
        }
    }
}
