package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.kazurayam.materialstore.util.GsonHelper
import com.kazurayam.materialstore.util.JsonUtil

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
    Set<String> keySet() {
        return new HashSet<String>(keySet)
    }

    @Override
    boolean contains(String key) {
        return keySet.contains(key)
    }

    @Override
    Iterator<String> iterator() {
        keySet.iterator()
    }

    //------------------------- override java.lang.Object ------------------
    @Override
    String toString() {
        return toJson()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }


    @Override
    String toJson() {
        List<String> list = new ArrayList<String>(keySet)
        Collections.sort(list)
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        list.each {
            if (count > 0) {
                sb.append(", ")
            }
            sb.append("\"")
            sb.append(JsonUtil.escapeAsJsonString(it))
            sb.append("\"")
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }

}
