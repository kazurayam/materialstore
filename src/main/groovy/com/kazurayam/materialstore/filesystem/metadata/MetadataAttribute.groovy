package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.kazurayam.materialstore.filesystem.Jsonifiable
import com.kazurayam.materialstore.filesystem.TemplateReady
import com.kazurayam.materialstore.util.GsonHelper
import com.kazurayam.materialstore.util.JsonUtil

/**
 *
 */
class MetadataAttribute implements Comparable<MetadataAttribute>, Jsonifiable, TemplateReady {

    private final String key
    private final String value
    private boolean ignoredByKey = false
    private boolean identifiedByValue = false
    private boolean matchedByAster = false
    private boolean matchedIndividually = false
    private boolean paired = false
    private SemanticVersionMatcherResult semanticVersionMatcherResult = null

    MetadataAttribute(String k, String v) {
        this.key = k
        this.value = v
    }

    void setIgnoredByKey(boolean b) {
        this.ignoredByKey = b
    }

    void setIdentifiedByValue(boolean b) {
        this.identifiedByValue = b
    }

    void setMatchedByAster(boolean b) {
        this.matchedByAster = b
    }

    void setMatchedIndividually(boolean b) {
        this.matchedIndividually = b
    }

    void setPaired(boolean b) {
        this.paired = b
    }

    void setSemanticVersionMatcherResult(result) {
        this.semanticVersionMatcherResult = result
    }

    String getKey() {
        return this.key
    }

    String getValue() {
        return this.value
    }

    boolean isIgnoredByKey() {
        return this.ignoredByKey
    }

    boolean isIdentifiedByValue() {
        return this.identifiedByValue
    }

    boolean isMatchedByAster() {
        return this.matchedByAster
    }

    boolean isMatchedIndividually() {
        return this.matchedIndividually
    }

    boolean isPaired() {
        return this.paired
    }

    SemanticVersionMatcherResult getSemanticVersionMatcherResult() {
        return this.semanticVersionMatcherResult
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MetadataAttribute) {
            return false
        }
        MetadataAttribute other = (MetadataAttribute)obj
        return this.getKey() == other.getKey() &&
                this.getValue() == other.getValue()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getKey().hashCode()
        hash = 31 * hash + this.getValue().hashCode()
        return hash
    }

    @Override
    String toString() {
        return getValue()
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
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"key\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getKey()) + "\"")
        sb.append(",")
        sb.append("\"value\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(value) + "\"")
        if (isIgnoredByKey()) {
            sb.append(",")
            sb.append("\"ignoredByKey\":")
            sb.append("true")
        }
        if (isIdentifiedByValue()) {
            sb.append(",")
            sb.append("\"identifiedByValue\":")
            sb.append("true")
        }
        if (isMatchedByAster()) {
            sb.append(",")
            sb.append("\"matchedByAster\":")
            sb.append("true")
        }
        if (isMatchedIndividually()) {
            sb.append(",")
            sb.append("\"matchedIndividually\":")
            sb.append("true")
        }
        if (isPaired()) {
            sb.append(",")
            sb.append("\"paired\":")
            sb.append("true")
        }
        if (getSemanticVersionMatcherResult() != null) {
            sb.append(",")
            sb.append("\"semanticVersionMatcherResult\":")
            sb.append(getSemanticVersionMatcherResult().toJson())
        }
        sb.append("}")
        return sb.toString()
    }

    @Override
    int compareTo(MetadataAttribute other) {
        int keyComparison = this.getKey() <=> other.getKey()
        if (keyComparison == 0) {
            return this.getValue() <=> other.getValue()
        } else {
            return keyComparison
        }
    }

    @Override
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    @Override
    String toTemplateModelAsJson() {
        return toTemplateModelAsJson(false)
    }

    @Override
    String toTemplateModelAsJson(boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint)
        Map<String, Object> model = toTemplateModel()
        return gson.toJson(model)
    }
}

