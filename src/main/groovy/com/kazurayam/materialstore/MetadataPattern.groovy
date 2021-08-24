package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder

import java.util.regex.Pattern

abstract class MetadataPattern implements MapLike {

    public static final MetadataPattern NULL_OBJECT = new Builder([:]).build()

    public static final MetadataPattern ANY = new Builder(["*": Pattern.compile(".*")]).build()

    /**
     * emit a HTML fragment (like the following) into the argument MarkupBuilder
     *
     * <span>{"profile":</span><span class="matched-value">"ProductionEnv"</span><span>}</span>
     *
     * @param mb
     */
    abstract void toSpanSequence(MarkupBuilder mb)

    abstract String getValueAsString(String key)

    static Builder builder() {
        return new Builder()
    }

    static Builder builderWithMap(Map<String, Object> source) {
        return new Builder(source)
    }

    static Builder builderWithMetadata(Metadata metadata) {
        return new Builder(metadata)
    }

    static Builder builderWithMetadata(Metadata metadata,
                                       IgnoringMetadataKeys ignoringMetadataKeys) {
        return new Builder(metadata, ignoringMetadataKeys)
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
        Builder(Metadata source) {
            this(source, IgnoringMetadataKeys.NULL_OBJECT)
        }
        Builder(Metadata source, IgnoringMetadataKeys ignoringMetadataKeys) {
            this()
            Objects.requireNonNull(ignoringMetadataKeys)
            Objects.requireNonNull(source)
            source.keySet().each {key ->
                Object value = source.get(key)
                if (value instanceof String || value instanceof Pattern) {
                    if (!ignoringMetadataKeys.contains(key)) {
                        metadataPattern.put(key, value)
                    }
                } else {
                    throw new MaterialstoreException(
                            "value(${value}) must be an instance of java.lang.String or java.util.regex.Pattern")
                }
            }
        }
        Builder put(String key, Object value) {
            metadataPattern.put(key, value)
            return this
        }
        MetadataPattern build() {
            return new MetadataPatternImpl(metadataPattern)
        }
    }
}
