package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.filesystem.Jsonifiable;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import com.kazurayam.materialstore.util.JsonUtil;

/**
 *
 */
public final class MetadataAttribute implements Comparable<MetadataAttribute>, Jsonifiable, TemplateReady {

    private final String key;
    private final String value;
    private boolean ignoredByKey = false;
    private boolean identifiedByValue = false;
    private boolean matchedByAster = false;
    private boolean matchedIndividually = false;
    private boolean paired = false;
    private SemanticVersionMatcherResult semanticVersionMatcherResult = null;

    public MetadataAttribute(String k, String v) {
        this.key = k;
        this.value = v;
    }

    public void setIgnoredByKey(boolean b) {
        this.ignoredByKey = b;
    }

    public void setIdentifiedByValue(boolean b) {
        this.identifiedByValue = b;
    }

    public void setMatchedByAster(boolean b) {
        this.matchedByAster = b;
    }

    public void setMatchedIndividually(boolean b) {
        this.matchedIndividually = b;
    }

    public void setPaired(boolean b) {
        this.paired = b;
    }

    public void setSemanticVersionMatcherResult(SemanticVersionMatcherResult result) {
        this.semanticVersionMatcherResult = result;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isIgnoredByKey() {
        return this.ignoredByKey;
    }

    public boolean isIdentifiedByValue() {
        return this.identifiedByValue;
    }

    public boolean isMatchedByAster() {
        return this.matchedByAster;
    }

    public boolean isMatchedIndividually() {
        return this.matchedIndividually;
    }

    public boolean isPaired() {
        return this.paired;
    }

    public SemanticVersionMatcherResult getSemanticVersionMatcherResult() {
        return this.semanticVersionMatcherResult;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MetadataAttribute)) {
            return false;
        }

        MetadataAttribute other = (MetadataAttribute) obj;
        return this.getKey().equals(other.getKey()) && this.getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getKey().hashCode();
        hash = 31 * hash + this.getValue().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return getValue();
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
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"key\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(this.getKey()));
        sb.append("\"");
        sb.append(",");
        sb.append("\"value\":");
        sb.append("\"");
        sb.append(JsonUtil.escapeAsJsonString(value));
        sb.append("\"");
        if (isIgnoredByKey()) {
            sb.append(",");
            sb.append("\"ignoredByKey\":");
            sb.append("true");
        }

        if (isIdentifiedByValue()) {
            sb.append(",");
            sb.append("\"identifiedByValue\":");
            sb.append("true");
        }

        if (isMatchedByAster()) {
            sb.append(",");
            sb.append("\"matchedByAster\":");
            sb.append("true");
        }

        if (isMatchedIndividually()) {
            sb.append(",");
            sb.append("\"matchedIndividually\":");
            sb.append("true");
        }

        if (isPaired()) {
            sb.append(",");
            sb.append("\"paired\":");
            sb.append("true");
        }

        if (getSemanticVersionMatcherResult() != null) {
            sb.append(",");
            sb.append("\"semanticVersionMatcherResult\":");
            sb.append(getSemanticVersionMatcherResult().toJson());
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(MetadataAttribute other) {
        int keyComparison = this.getKey().compareTo(other.getKey());
        if (keyComparison == 0) {
            return this.getValue().compareTo(other.getValue());
        } else {
            return keyComparison;
        }

    }

}
