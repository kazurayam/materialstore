package com.kazurayam.materialstore

class MetadataIgnoredKeys {

    public static final MetadataIgnoredKeys NULL_OBJECT = new Builder().build()

    private Set<String> keySet

    private MetadataIgnoredKeys(Builder builder) {
        this.keySet = builder.keySet
    }

    int size() {
        return keySet.size()
    }

    boolean contains(String key) {
        return keySet.contains(key)
    }

    Iterator<String> iterator() {
        keySet.iterator()
    }

    static class Builder {
        Set<String> keySet
        Builder(Metadata metadata) {
            this()
            this.keySet.addAll(metadata.keySet())
        }
        Builder() {
            this.keySet = new HashSet<String>()
        }
        Builder ignoreKey(String key) {
            this.keySet.add(key)
            return this
        }
        MetadataIgnoredKeys build() {
            return new MetadataIgnoredKeys(this)
        }
    }
}
