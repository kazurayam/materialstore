package com.kazurayam.materialstore

import groovy.json.JsonOutput

final class MetadataIgnoredKeysImpl extends MetadataIgnoredKeys {

    private Set<String> keySet

    MetadataIgnoredKeysImpl(Set<String> source) {
        this.keySet = source
    }

    //------------- MetadataIgnoredKeys ------------------
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
