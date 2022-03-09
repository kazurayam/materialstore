package com.kazurayam.materialstore.filesystem.metadata

import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder

final class IgnoreMetadataKeysImpl extends IgnoreMetadataKeys {

    private Set<String> keySet

    IgnoreMetadataKeysImpl(Set<String> source) {
        this.keySet = source
    }

    //------------- IgnoreMetadataKeys ------------------
    @Override
    int size() {
        return keySet.size()
    }

    @Override
    boolean contains(String key) {
        return keySet.contains(key)
    }

    @Override
    Iterator<String> iterator() {
        keySet.iterator()
    }

    @Override
    void toSpanSequence(MarkupBuilder mb) {
        List<String> list = new ArrayList<String>(keySet)
        Collections.sort(list)
        int count = 0
        mb.span("{")
        list.each {
            if (count > 0) {
                mb.span(", ")
            }
            mb.span(class: "ignored-key",
                    "\"" + JsonUtil.escapeAsJsonString(it) + "\"")
            count += 1
        }
        mb.span("}")
    }

    //------------------------- override java.lang.Object ------------------
    @Override
    String toString() {
        List<String> list = new ArrayList<String>(keySet)
        Collections.sort(list)
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("{")
        list.each {
            if (count > 0) {
                sb.append(", ")
            }
            sb.append("\"")
            sb.append(JsonUtil.escapeAsJsonString(it))
            sb.append("\"")
            count += 1
        }
        sb.append("}")
        return sb.toString()
    }
}
