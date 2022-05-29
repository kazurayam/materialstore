package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.util.JsonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class IgnoreMetadataKeysImpl extends IgnoreMetadataKeys {

    private Set<String> keySet;

    public IgnoreMetadataKeysImpl(Set<String> source) {
        this.keySet = source;
    }

    @Override
    public int size() {
        return keySet.size();
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>(keySet);
    }

    @Override
    public boolean contains(String key) {
        return keySet.contains(key);
    }

    @Override
    public Iterator<String> iterator() {
        return keySet.iterator();
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }

    }

    @Override
    public String toJson() {
        List<String> list = new ArrayList<>(keySet);
        Collections.sort(list);
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        sb.append("[");
        for (String str : list) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append("\"");
            sb.append(JsonUtil.escapeAsJsonString(str));
            sb.append("\"");
            count += 1;
        }
        sb.append("]");
        return sb.toString();
    }
}
