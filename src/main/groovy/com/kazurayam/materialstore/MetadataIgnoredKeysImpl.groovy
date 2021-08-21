package com.kazurayam.materialstore

class MetadataIgnoredKeysImpl extends MetadataIgnoredKeys {

    private Set<String> keySet

    MetadataIgnoredKeysImpl(Set<String> source) {
        this.keySet = source
    }

    //------------- MetadataIgnoredKeys ------------------
    @Override
    int size() {
        return keySet.size()
    }

    @Override
    boolean contains(String key) {
        return keySet.contains(key)
    }

    @Override
    Iterator<String> iterator() {
        keySet.iterator()
    }
}
