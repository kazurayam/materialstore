package com.kazurayam.materialstore

import java.util.regex.Pattern

class MetadataPattern implements MapLike {

    public static final MetadataPattern NULL_OBJECT = new Builder([:]).build()

    public static final MetadataPattern ANY = new Builder(["*": Pattern.compile(".*")]).build()

    private final Map<String, Object> metadataPattern

    private MetadataPattern(Builder builder) {
        this.metadataPattern = builder.metadataPattern
    }

    //------------- implements MapLike ----------------

    @Override
    boolean containsKey(String key) {
        return metadataPattern.containsKey(key)
    }

    @Override
    Object get(String key) {
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
        List<String> keyList = new ArrayList(metadataPattern.keySet())
        Collections.sort(keyList)
        int count = 0
        sb.append("{")
        keyList.forEach({key ->
            if (count > 0) {
                sb.append(", ")   // one whitespace after , is significant
            }
            sb.append("\"")
            sb.append(key)
            sb.append("\":\"")
            if (metadataPattern.get(key) instanceof Pattern) {
                sb.append("regex:")
            }
            sb.append(metadataPattern.get(key))
            sb.append("\"")
            count += 1
        })
        sb.append("}")
        return sb.toString()
    }

    static class Builder {
        private Map<String, Object> metadataPattern
        Builder() {
            this.metadataPattern = new HashMap<String, Object>()
        }
        Builder(Map<String, Object> map) {
            this()
            Objects.requireNonNull(map)
            map.keySet().each { key ->
                Object value = map.get(key)
                if (value instanceof String || value instanceof Pattern) {
                    metadataPattern.put(key, map.get(key))
                } else {
                    throw new MaterialstoreException(
                            "value(${value}) must be an instance of java.lang.String or java.util.regex.Pattern")
                }
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
                Object value = source.get(key)
                if (value instanceof String || value instanceof Pattern) {
                    if (!ignoredKeys.contains(key)) {
                        metadataPattern.put(key, value)
                    }
                } else {
                    throw new MaterialstoreException(
                            "value(${value}) must be an instance of java.lang.String or java.util.regex.Pattern")
                }
            }
        }
        MetadataPattern build() {
            return new MetadataPattern(this)
        }
    }
}
