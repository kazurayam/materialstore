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

    private String key = null
    private String value = null
    private boolean ignoredByKey = false
    private boolean identifiedByValue = false
    private boolean matchedByAster = false
    private boolean matchedIndividually = false
    private boolean paired = false
    private String semanticVersion = null

    MetadataAttribute(String key) {
        this(key, null)
    }

    MetadataAttribute(String key, String value) {
        this.key = key
        this.value = value
    }

    void setValue(String value) {
        this.value = value
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

    void setSemanticVersion(String version) {
        this.semanticVersion = version
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

    String getSemanticVersion() {
        return this.semanticVersion
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
        return toJson()
    }

    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getKey()) + "\"")
        sb.append(": ")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getValue()) + "\"")
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }

    String toRichJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"key\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getKey()) + "\"")
        sb.append(",")
        sb.append("\"value\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getValue()) + "\"")
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
        if (getSemanticVersion() != null) {
            sb.append(",")
            sb.append("\"semanticVersion\":")
            sb.append("\"" + JsonUtil.escapeAsJsonString(this.getSemanticVersion()) + "\"")
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
        Map<String, Object> map = new Gson().fromJson(toRichJson(), Map.class)
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

