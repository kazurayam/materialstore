package com.kazurayam.materialstore

class MetadataPattern implements MapLike {

    public static final MetadataPattern NULL_OBJECT = new Builder([:]).build()

    public static final MetadataPattern ANY = new Builder(["*": "*"]).build()

    private final Map<String, String> metadataPattern

    private MetadataPattern(Builder builder) {
        this.metadataPattern = builder.metadataPattern
    }

    //------------- implements MapLike ----------------

    @Override
    boolean containsKey(String key) {
        return metadataPattern.containsKey(key)
    }

    @Override
    String get(String key) {
        return metadataPattern.get(key)
    }

    @Override
    boolean isEmpty() {
        return metadataPattern.isEmpty()
    }

    @Override
    Set<String> keySet() {
        return metadataPattern.keySet()
    }

    @Override
    int size() {
        return metadataPattern.size()
    }

    //------------- implements java.lang.Object -------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        Set<String> keySet = metadataPattern.keySet()
        int count = 0
        sb.append("{")
        keySet.forEach({key ->
            if (count > 0) {
                sb.append(", ")
            }
            sb.append("\"")
            sb.append(key)
            sb.append("\":\"")
            sb.append(metadataPattern.get(key))
            sb.append("\"")
            count += 1
        })
        sb.append("}")
        return sb.toString()
    }

    static class Builder {
        private Map<String, String> metadataPattern
        Builder() {
            this.metadataPattern = new HashMap<String, String>()
        }
        Builder(Map<String, String> map) {
            this()
            Objects.requireNonNull(map)
            map.keySet().each { key ->
                metadataPattern.put(key, map.get(key))
            }
        }
        Builder(MapLike source) {
            this(source, MetadataIgnoredKeys.NULL_OBJECT)
        }
        Builder(MapLike source, MetadataIgnoredKeys ignoredKeys) {
            this()
            Objects.requireNonNull(ignoredKeys)
            Objects.requireNonNull(source)
            source.keySet().each {key ->
                if (! ignoredKeys.contains(key)) {
                    metadataPattern.put(key, source.get(key))
                }
            }
        }
        MetadataPattern build() {
            return new MetadataPattern(this)
        }
    }
}
