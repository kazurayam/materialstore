package com.kazurayam.materialstore.metadata


import groovy.xml.MarkupBuilder

import java.util.regex.Pattern

abstract class QueryOnMetadata {

    public static final QueryOnMetadata NULL_OBJECT =
            new Builder().build()

    public static final QueryOnMetadata ANY =
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
                                       IgnoreMetadataKeys ignoreMetadataKeys) {
        return new Builder(metadata, ignoreMetadataKeys)
    }

    //-----------------------------------------------------------------

    abstract boolean containsKey(String key)

    abstract Set<Entry> entrySet()

    abstract QueryOnMetadataValue get(String key)

    abstract String getDescription(SortKeys sortKeys)

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

        private Map<String, QueryOnMetadataValue> query

        Builder() {
            this.query = new HashMap<String, QueryOnMetadataValue>()
        }

        Builder(Map<String, String> map) {
            this()
            Objects.requireNonNull(map)
            map.keySet().each { key ->
                query.put(
                        key,
                        QueryOnMetadataValue.of(map.get(key))
                )
            }
        }
        Builder(Metadata source) {
            this(source, IgnoreMetadataKeys.NULL_OBJECT)
        }
        Builder(Metadata source,
                IgnoreMetadataKeys ignoreMetadataKeys) {
            this()
            Objects.requireNonNull(ignoreMetadataKeys)
            Objects.requireNonNull(source)
            //
            source.keySet().each {key ->
                QueryOnMetadataValue mpv = QueryOnMetadataValue.of((String)source.get(key))
                if (!ignoreMetadataKeys.contains(key)) {
                    query.put(key, mpv)
                }
            }
        }
        Builder(QueryOnMetadata source) {
            this()
            Objects.requireNonNull(source)
            source.keySet().each {key ->
                QueryOnMetadataValue mpv = new QueryOnMetadataValue.Builder(source.get(key)).build()
                query.put(key, mpv)
            }
        }
        Builder put(String key, String value) {
            query.put(
                    key,
                    QueryOnMetadataValue.of(value)
            )
            return this
        }
        Builder put(String key, Pattern value) {
            query.put(
                    key,
                    QueryOnMetadataValue.of(value)
            )
            return this
        }
        QueryOnMetadata build() {
            return new QueryOnMetadataImpl(query)
        }
    }

    /**
     * a pair of Key-Value in the QueryOnMetadata object.
     * This class implements boolean matches(Metadata) method, which works
     * as a helper for QueryOnMetadataImpl#matches(Metadata) method.
     */
    static class Entry implements Comparable {
        private String key
        private QueryOnMetadataValue query
        Entry(String key, QueryOnMetadataValue query) {
            this.key = key
            this.query = query
        }
        String getKey() {
            return this.key
        }
        QueryOnMetadataValue getQueryOnMetadataValue() {
            return this.query
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
                    if (this.queryOnMetadataValue.matches(metadata.get(metadataKey))) {
                        found = true
                    }
                }
                return found
            } else if (metadata.containsKey(key)) {
                return this.query.matches(metadata.get(key))
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
                return this.query <=> other.queryOnMetadataValue
            } else
                return keyComp
        }
    }
}
