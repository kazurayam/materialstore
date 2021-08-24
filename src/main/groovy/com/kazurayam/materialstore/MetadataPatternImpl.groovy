package com.kazurayam.materialstore

import groovy.xml.MarkupBuilder

import java.util.regex.Pattern

final class MetadataPatternImpl extends MetadataPattern implements MapLike {

    private final Map<String, Object> metadataPattern

    MetadataPatternImpl(Map<String, Object> source) {
        this.metadataPattern = source
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

    //------------- implements MetadataPattern --------
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
            mb.span("\"${key}\":")
            mb.span("class": "matched-value", "\"" + this.getValueAsString(key) + "\"")
            count += 1
        })
        mb.span("}")
    }

    /**
     * return "value" if the value is a String
     * return "re:value" if the value is a Pattern
     *
     * @param key
     * @return
     */
    @Override
    String getValueAsString(String key) {
        def value = this.get(key)
        if (value instanceof String)
            return (String)value
        else if (value instanceof Pattern) {
            return 're:' + ((Pattern)value).toString()
        } else {
            throw new IllegalStateException("value is instance of ${value.class.getName()}, which is unexpected")
        }
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
            sb.append(key)
            sb.append("\":\"")
            sb.append(this.getValueAsString(key))
            sb.append("\"")
            count += 1
        })
        sb.append("}")
        return sb.toString()
    }

    // -------------------------------------------
}
