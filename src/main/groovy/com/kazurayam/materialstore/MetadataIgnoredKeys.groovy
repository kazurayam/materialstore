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


    private static class Builder {
        Set<String> keySet
        /*
        Builder(Metadata metadata) {
            this()
            this.keySet.addAll(metadata.keySet())
        }
        Builder(MetadataPattern metadataPattern) {
            this()
            this.keySet.addAll(metadataPattern.keySet())
        }
         */
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
