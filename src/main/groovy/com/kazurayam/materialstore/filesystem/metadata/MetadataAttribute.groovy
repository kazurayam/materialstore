package com.kazurayam.materialstore.filesystem.metadata

import com.google.gson.Gson
import com.kazurayam.materialstore.filesystem.JSONifiable
import com.kazurayam.materialstore.filesystem.TemplateReady
import com.kazurayam.materialstore.util.JsonUtil

/**
 *
 */
class MetadataAttribute implements Comparable<MetadataAttribute>, JSONifiable, TemplateReady {

    private String key = null
    private Object value = null
    private boolean ignoredByKey = false
    private boolean identifiedByValue = false
    private String semanticVersion = null

    MetadataAttribute(String key) {
        this(key, null)
    }

    MetadataAttribute(String key, Object value) {
        this.key = key
        this.value = value
    }

    void setValue(Object value) {
        this.value = value
    }

    void setIgnoredByKey(boolean b) {
        this.ignoredByKey = b
    }

    void setIdentifiedByValue(boolean b) {
        this.identifiedByValue = b
    }

    void setSemanticVersion(String version) {
        this.semanticVersion = version
    }

    String getKey() {
        return this.key
    }

    Object getValue() {
        return this.value
    }

    boolean isIgnoredByKey() {
        return this.ignoredByKey
    }

    boolean isIdentifiedByValue() {
        return this.identifiedByValue
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
                this.getValue() == other.getValue() &&
                this.isIgnoredByKey() == other.isIgnoredByKey() &&
                this.isIdentifiedByValue() == other.isIdentifiedByValue() &&
                this.getSemanticVersion() == other.getSemanticVersion()
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
        sb.append("\"key\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getKey()) + "\"")
        sb.append(",")
        sb.append("\"value\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(this.getValue().toString()) + "\"")
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
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }
}

