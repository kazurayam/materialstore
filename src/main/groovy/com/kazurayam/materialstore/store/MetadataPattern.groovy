package com.kazurayam.materialstore.store

class MetadataPattern extends Metadata {

    public static final MetadataPattern NULL_OBJECT = new MetadataPattern([:])

    public static final MetadataPattern ANY = new MetadataPattern(["*": "*"])

    static MetadataPattern create(Metadata source) {
        return create(MetadataIgnoredKeys.NULL_OBJECT, source)
    }

    static MetadataPattern create(MetadataIgnoredKeys ignoredKeys, Metadata source) {
        Objects.requireNonNull(ignoredKeys)
        Objects.requireNonNull(source)
        Map<String, String> m = new HashMap<String, String>()
        source.keySet().each {key ->
            if (! ignoredKeys.contains(key)) {
                m.put(key, source.get(key))
            }
        }
        return new MetadataPattern(m)
    }

    MetadataPattern(Map<String, String> entries) {
        super(entries)
    }

}
