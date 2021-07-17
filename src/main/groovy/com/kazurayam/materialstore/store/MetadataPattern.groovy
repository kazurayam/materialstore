package com.kazurayam.materialstore.store

class MetadataPattern extends Metadata {

    static MetadataPattern create(Set<String> keys, Metadata source) {
        Objects.requireNonNull(keys)
        Objects.requireNonNull(source)
        Map<String, String> m = new HashMap<String, String>()
        keys.each {key ->
            if (source.containsKey(key)) {
                m.put(key, source.get(key))
            } else {
                m.put(key, "*")
            }
        }
        return new MetadataPattern(m)
    }

    MetadataPattern(Map<String, String> entries) {
        super(entries)
    }

}