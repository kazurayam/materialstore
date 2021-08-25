package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder

import java.util.regex.Pattern

final class MetadataPatternImpl extends MetadataPattern {

    private final Map<String, MetadataPatternValue> metadataPattern

    MetadataPatternImpl(Map<String, MetadataPatternValue> source) {
        this.metadataPattern = source
    }

    //------------- implements MapLike ----------------

    @Override
    boolean containsKey(String key) {
        return metadataPattern.containsKey(key)
    }

    @Override
    boolean containsKey(Pattern key) {
        return metadataPattern.containsKey(key)
    }

    @Override
    MetadataPatternValue get(String key) {
        return metadataPattern.get(key)
    }

    @Override
    String getAsString(String key) {
        return this.get(key).toString()
    }

    @Override
    MetadataPatternValue get(Pattern key) {
        return metadataPattern.get(key)
    }

    @Override
    String getAsString(Pattern key) {
        return this.get(key)
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

    Set<MetadataPatternEntry> entrySet() {
        Set<MetadataPatternEntry> entrySet = new HashSet<MetadataPatternEntry>()
        this.keySet().forEach {key ->
            MetadataPatternValue mpv = this.get(key)
            MetadataPatternEntry entry = new MetadataPatternEntry(key, mpv)
            entrySet.add(entry)
        }
        return entrySet
    }

    //------------- implements MetadataPattern --------
    /**
     * Returns true if this MetadataPattern has one or more entries that matches with
     * one or more entries in the given Metadata.
     * Returns false if this MetadataPattern has no entry that matches with
     * any of entries in the given Metadata.
     *
     * @param metadata
     * @return
     */
    @Override
    boolean matches(Metadata metadata) {
        Set<MetadataPatternEntry> entrySet = this.entrySet()
        boolean result = true
        entrySet.each {entry ->
            if (! entry.matches(metadata)) {
                result = false
                return
            }
        }
        return result
    }


    @Override
    void toSpanSequence(MarkupBuilder mb) {
        List<String> keyList = new ArrayList(metadataPattern.keySet())
        Collections.sort(keyList)
        int count = 0
        mb.span("{")
        keyList.forEach( { String key ->
            if (count > 0) {
                mb.span(", ")
            }
            mb.span("\"${key.toString()}\":")
            mb.span("class": "matched-value", "\"" + this.getAsString(key) + "\"")
            count += 1
        })
        mb.span("}")
    }

    //------------- implements java.lang.Object -------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        List<String> keyList = new ArrayList(metadataPattern.keySet())
        Collections.sort(keyList)
        int count = 0
        sb.append("{")
        keyList.forEach({ String key ->
            if (count > 0) {
                sb.append(", ")   // one whitespace after , is significant
            }
            sb.append("\"")
            sb.append(key.toString())
            sb.append("\":\"")
            MetadataPatternValue value = this.get(key)
            sb.append(value.toString())
            sb.append("\"")
            count += 1
        })
        sb.append("}")
        return sb.toString()
    }

    /**
     *
     */
    class MetadataPatternEntry {
        private String key
        private MetadataPatternValue metadataPatternValue
        MetadataPatternEntry(String key, MetadataPatternValue metadataPatternValue) {
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
    }
}
