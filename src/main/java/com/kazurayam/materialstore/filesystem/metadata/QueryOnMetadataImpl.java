package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.util.JsonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class QueryOnMetadataImpl extends QueryOnMetadata {

    private final Map<String, QValue> keyQValuePairs;

    public QueryOnMetadataImpl(Map<String, QValue> source) {
        this.keyQValuePairs = source;
    }

    @Override
    public boolean containsKey(String key) {
        return keyQValuePairs.containsKey(key);
    }

    @Override
    public QValue get(String key) {
        return keyQValuePairs.get(key);
    }

    @Override
    public String getAsString(String key) {
        return this.get(key).toString();
    }

    @Override
    public boolean isEmpty() {
        return keyQValuePairs.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return keyQValuePairs.keySet();
    }

    @Override
    public int size() {
        return keyQValuePairs.size();
    }

    @Override
    public Set<QEntry> entrySet() {
        final Set<QEntry> entrySet = new HashSet<>();
        for (String key : this.keySet()) {
            QValue qValue = this.get(key);
            QEntry entry = new QEntry(key, qValue);
            entrySet.add(entry);
        }
        return entrySet;
    }

    /**
     * Returns true if this QueryOnMetadata has one or more entries that matches with
     * one or more entries in the given Metadata.
     * Returns false if this QueryOnMetadata has no entry that matches with
     * any of entries in the given Metadata.
     *
     */
    @Override
    public boolean matches(final Metadata metadata) {
        Set<QEntry> entrySet = this.entrySet();
        boolean result = true;
        for (QEntry entry : entrySet) {
            if (!entry.matches(metadata)) {
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> toJSONTextTokens() {
        final List<Map<String, String>> jsonTextTokens = new ArrayList<>();
        //
        List<String> keyList = new ArrayList<>(keyQValuePairs.keySet());
        Collections.sort(keyList);
        int count = 0;
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("text", "{");
        jsonTextTokens.add(map);
        for (String key : keyList) {
            if (count > 0) {
                LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
                map1.put("text", ",");
                jsonTextTokens.add(map1);
            }
            LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
            map1.put("text", "\"" + key + "\":");
            jsonTextTokens.add(map1);
            LinkedHashMap<String, String> map2 = new LinkedHashMap<>(2);
            map2.put("class", "matched-value");
            map2.put("text", "\"" + getAsString(key) + "\"");
            jsonTextTokens.add(map2);
            count += 1;
        }
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("text", "}");
        jsonTextTokens.add(map1);
        return jsonTextTokens;
    }

    @Override
    public String toString() {
        return getDescription(SortKeys.NULL_OBJECT);
    }

    @Override
    public String toJson() {
        return getDescription(SortKeys.NULL_OBJECT);
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
    public String getDescription(SortKeys sortKeys) {
        List<String> keyList = orderedKeyList(keyQValuePairs.keySet(), sortKeys);
        //
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        sb.append("{");
        for (String key : keyList) {
            if (count > 0) {
                sb.append(", ");// one whitespace after , is significant
            }
            sb.append("\"");
            sb.append(key);
            sb.append("\":\"");
            QValue qValue = this.get(key);
            sb.append(qValue.toString());
            sb.append("\"");
            count += 1;
        }
        sb.append("}");
        // WARNING: must not pretty-print this.
        return sb.toString();
    }

    private List<String> orderedKeyList(Set<String> keySet, SortKeys sortKeys) {
        if (sortKeys.equals(SortKeys.NULL_OBJECT)) {
            List<String> keyList = new ArrayList<>(keyQValuePairs.keySet());
            Collections.sort(keyList);
            return keyList;
        } else {
            final Set<String> workSet = new HashSet<>(keySet);
            final List<String> orderedKeyList = new ArrayList<>();
            Iterator<String> iter = sortKeys.iterator();
            while (iter.hasNext()){
                String key = iter.next();
                if (workSet.contains(key)) {
                    orderedKeyList.add(key);
                    workSet.remove(key);
                }
            }
            List<String> workList = new ArrayList<>(workSet);
            Collections.sort(workList);
            orderedKeyList.addAll(workList);
            return orderedKeyList;
        }
    }
}
