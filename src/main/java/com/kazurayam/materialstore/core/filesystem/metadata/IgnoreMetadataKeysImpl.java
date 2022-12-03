package com.kazurayam.materialstore.core.filesystem.metadata;

import com.kazurayam.materialstore.core.util.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean add(String s) {
        return this.keySet.add(s);
    }

    @Override
    public boolean addAll(String... args) {
        return this.addAll(Arrays.asList(args));
    }

    @Override
    public boolean addAll(List<String> list) {
        return this.keySet.addAll(list);
    }

    @Override
    public boolean addAll(Set<String> set) {
        return this.keySet.addAll(set);
    }

    @Override
    public boolean addAll(IgnoreMetadataKeys imk) {
        return this.keySet.addAll(imk.keySet());
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
