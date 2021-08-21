package com.kazurayam.materialstore

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

    // -------------------------------------------
}
