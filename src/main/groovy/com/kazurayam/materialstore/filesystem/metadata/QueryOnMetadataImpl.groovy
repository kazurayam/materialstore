package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata

final class QueryOnMetadataImpl extends QueryOnMetadata {

    private final Map<String, QValue> keyQValuePairs

    QueryOnMetadataImpl(Map<String, QValue> source) {
        this.keyQValuePairs = source
    }

    //------------- implements MapLike ----------------

    @Override
    boolean containsKey(String key) {
        return keyQValuePairs.containsKey(key)
    }

    @Override
    QValue get(String key) {
        return keyQValuePairs.get(key)
    }

    @Override
    String getAsString(String key) {
        return this.get(key).toString()
    }

    @Override
    boolean isEmpty() {
        return keyQValuePairs.isEmpty()
    }

    @Override
    Set<String> keySet() {
        return keyQValuePairs.keySet()
    }

    @Override
    int size() {
        return keyQValuePairs.size()
    }

    @Override
    Set<Entry> entrySet() {
        Set<Entry> entrySet = new HashSet<Entry>()
        this.keySet().forEach {key ->
            QValue qValue = this.get(key)
            Entry entry = new Entry(key, qValue)
            entrySet.add(entry)
        }
        return entrySet
    }

    //------------- implements QueryOnMetadata --------
    /**
     * Returns true if this QueryOnMetadata has one or more entries that matches with
     * one or more entries in the given Metadata.
     * Returns false if this QueryOnMetadata has no entry that matches with
     * any of entries in the given Metadata.
     *
     * @param metadata
     * @return
     */
    @Override
    boolean matches(Metadata metadata) {
        Set<Entry> entrySet = this.entrySet()
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
    List<Map<String,String>> toJSONTextTokens() {
        List<Map<String, String>> jsonTextTokens = new ArrayList<>()
        //
        List<String> keyList = new ArrayList(keyQValuePairs.keySet())
        Collections.sort(keyList)
        int count = 0
        jsonTextTokens.add(["text": "{"])
        keyList.forEach({ String key ->
            if (count > 0) {
                jsonTextTokens.add(["text": ","])
            }
            jsonTextTokens.add(["text" : "\"" + key.toString() + "\":"])
            jsonTextTokens.add(["class" : "matched-value",
                                "text": "\"" + getAsString(key) + "\""])
            count += 1
        })
        jsonTextTokens.add(["text": "}"])
        return jsonTextTokens
    }

    //------------- implements java.lang.Object -------
    @Override
    String toString() {
        return getDescription(SortKeys.NULL_OBJECT)
    }

    @Override
    String toJson() {
        return getDescription(SortKeys.NULL_OBJECT)
    }

    @Override
    String getDescription(SortKeys sortKeys) {
        List<String> keyList = orderedKeyList(keyQValuePairs.keySet(), sortKeys)
        //
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("{")
        keyList.forEach({ String key ->
            if (count > 0) {
                sb.append(", ")   // one whitespace after , is significant
            }
            sb.append("\"")
            sb.append(key.toString())
            sb.append("\":\"")
            QValue qValue = this.get(key)
            sb.append(qValue.toString())
            sb.append("\"")
            count += 1
        })
        sb.append("}")
        // WARNING: must not pretty-print this.
        return sb.toString()
    }

    private List<String> orderedKeyList(Set<String> keySet, SortKeys sortKeys) {
        if (sortKeys == SortKeys.NULL_OBJECT) {
            List<String> keyList = new ArrayList<>(keyQValuePairs.keySet())
            Collections.sort(keyList)
            return keyList
        } else {
            Set<String> workSet = new HashSet<>(keySet)
            List<String> orderedKeyList = new ArrayList<>()
            sortKeys.each {k ->
                if (workSet.contains(k)) {
                    orderedKeyList.add(k)
                    workSet.remove(k)
                }
            }
            List<String> workList = new ArrayList<>(workSet)
            Collections.sort(workList)
            workList.each {k ->
                orderedKeyList.add(k)
            }
            return orderedKeyList
        }
    }

    @Override
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    @Override
    String toTemplateModelAsJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create()
        Map<String, Object> model = toTemplateModel()
        return gson.toJson(model)
    }
}
