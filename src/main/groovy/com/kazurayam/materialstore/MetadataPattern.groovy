package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder

import java.util.regex.Pattern

abstract class MetadataPattern {

    public static final MetadataPattern NULL_OBJECT =
            new Builder().build()

    public static final MetadataPattern ANY =
            new Builder().put(
                    "*",
                    Pattern.compile(".*")
            ).build()

    //-------------------------factory methods --------------------------------

    static Builder builder() {
        return new Builder()
    }

    static Builder builderWithMap(Map<String, String> source) {
        return new Builder(source)
    }

    static Builder builderWithMetadata(Metadata metadata) {
        return new Builder(metadata)
    }

    static Builder builderWithMetadata(Metadata metadata,
                                       IgnoringMetadataKeys ignoringMetadataKeys) {
        return new Builder(metadata, ignoringMetadataKeys)
    }

    //-----------------------------------------------------------------

    abstract boolean containsKey(String key)

    abstract Set<Entry> entrySet()

    abstract MetadataPatternValue get(String key)

    abstract String getAsString(String key)

    abstract boolean isEmpty()

    abstract Set<String> keySet()

    abstract boolean matches(Metadata metadata)

    abstract int size()

    /**
     * emit a HTML fragment (like the following) into the argument MarkupBuilder
     *
     * <span>{"profile":</span><span class="matched-value">"ProductionEnv"</span><span>}</span>
     *
     * @param mb
     */
    abstract void toSpanSequence(MarkupBuilder mb)


    /**
     *
     */
    static class Builder {

        private Map<String, MetadataPatternValue> metadataPattern

        Builder() {
            this.metadataPattern = new HashMap<String, MetadataPatternValue>()
        }

        Builder(Map<String, String> map) {
            this()
            Objects.requireNonNull(map)
            map.keySet().each { key ->
                String value = map.get(key)
                metadataPattern.put(
                        key,
                        MetadataPatternValue.of(map.get(key))
                )
            }
        }
        Builder(Metadata source) {
            this(source, IgnoringMetadataKeys.NULL_OBJECT)
        }
        Builder(Metadata source,
                IgnoringMetadataKeys ignoringMetadataKeys) {
            this()
            Objects.requireNonNull(ignoringMetadataKeys)
            Objects.requireNonNull(source)
            //
            source.keySet().each {key ->
                MetadataPatternValue mpv = MetadataPatternValue.of((String)source.get(key))
                if (!ignoringMetadataKeys.contains(key)) {
                    metadataPattern.put(key, mpv)
                }
            }
        }
        Builder put(String key, String value) {
            metadataPattern.put(
                    key,
                    MetadataPatternValue.of(value)
            )
            return this
        }
        Builder put(String key, Pattern value) {
            metadataPattern.put(
                    key,
                    MetadataPatternValue.of(value)
            )
            return this
        }
        MetadataPattern build() {
            return new MetadataPatternImpl(metadataPattern)
        }
    }

    /**
     * a pair of Key-Value in the MetadataPattern object.
     * This class implements boolean matches(Metadata) method, which works
     * as a helper for MetadataPatternImpl#matches(Metadata) method.
     */
    static class Entry implements Comparable {
        private String key
        private MetadataPatternValue metadataPatternValue
        Entry(String key, MetadataPatternValue metadataPatternValue) {
            this.key = key
            this.metadataPatternValue = metadataPatternValue
        }
        String getKey() {
            return this.key
        }
        MetadataPatternValue getMetadataPatternValue() {
            return this.metadataPatternValue
        }
        /**
         *
         * @param metadata
         * @return
         */
        boolean matches(Metadata metadata) {
            if (this.key == "*") {
                boolean found = false
                metadata.keySet().each {metadataKey ->
                    if (this.metadataPatternValue.matches(metadata.get(metadataKey))) {
                        found = true
                    }
                }
                return found
            } else if (metadata.containsKey(key)) {
                return this.metadataPatternValue.matches(metadata.get(key))
            } else {
                return false
            }
        }
        @Override
        int compareTo(Object obj) {
            if (!obj instanceof Entry) {
                throw new IllegalArgumentException("obj is not Entry")
            }
            Entry other = (Entry)obj
            def keyComp = this.key <=> other.key
            if (keyComp != 0) {
                return this.metadataPatternValue <=> other.metadataPatternValue
            } else
                return keyComp
        }
    }
}
