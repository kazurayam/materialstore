package com.kazurayam.materialstore

abstract class MetadataIgnoredKeys {

    public static final MetadataIgnoredKeys NULL_OBJECT = new Builder().build()

    abstract int size()

    abstract boolean contains(String key)

    abstract Iterator<String> iterator()

    // -------------------- factory methods ------------
    static MetadataIgnoredKeys of(String... keys) {
        Builder builder = new Builder()
        keys.each {
            builder.ignoreKey(it)
        }
        return builder.build()
    }


    static class Builder {
        Set<String> keySet
        Builder() {
            this.keySet = new HashSet<String>()
        }
        Builder ignoreKey(String key) {
            this.keySet.add(key)
            return this
        }
        MetadataIgnoredKeys build() {
            return new MetadataIgnoredKeysImpl(keySet)
        }
    }
}
