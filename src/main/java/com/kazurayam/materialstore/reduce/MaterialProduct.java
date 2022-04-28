package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.Identifiable;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialIO;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.differ.DifferUtil;
import com.kazurayam.materialstore.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * "Material x Material" = "Materials Product"
 * <p>
 * is used to carry data of a pair of "Material" objects,
 * plus the "diff" of the two.
 */
public final class MaterialProduct
        implements Comparable<MaterialProduct>, TemplateReady, Identifiable {

    public static final MaterialProduct NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                    JobTimestamp.NULL_OBJECT)
                    .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                    .build();

    private final Material left;
    private final Material right;
    private final JobTimestamp reducedTimestamp;
    private final QueryOnMetadata query;
    private final SortKeys sortKeys;
    private Boolean checked;
    private Material diff;
    private Double diffRatio;

    private MaterialProduct(Builder builder) {
        this.left = builder.left;
        this.right = builder.right;
        this.diff = builder.diff;
        this.reducedTimestamp = builder.reducedTimestamp;
        this.query = builder.query;
        this.diffRatio = builder.diffRatio;
        this.sortKeys = builder.sortKeys;
        this.checked = builder.checked;
    }

    /**
     * copy constructor
     *
     * @param source
     */
    public MaterialProduct(MaterialProduct source) {
        Objects.requireNonNull(source);
        this.left = source.getLeft();
        this.right = source.getRight();
        this.diff = source.getDiff();
        this.reducedTimestamp = source.getReducedTimestamp();
        this.query = source.getQueryOnMetadata();
        this.diffRatio = source.getDiffRatio();
        this.sortKeys = source.getSortKeys();
        this.checked = source.isChecked();
    }

    public void annotate(IgnoreMetadataKeys ignoreMetadataKeys, IdentifyMetadataValues identifyMetadataValues) {
        this.left.getMetadata().annotate(query, ignoreMetadataKeys, identifyMetadataValues);
        this.right.getMetadata().annotate(query, ignoreMetadataKeys, identifyMetadataValues);
    }

    public void setDiff(Material diff) {
        Objects.requireNonNull(diff);
        this.diff = diff;
    }

    public void setDiffRatio(Double diffRatio) {
        Objects.requireNonNull(diffRatio);
        this.diffRatio = diffRatio;
    }

    public Material getLeft() {
        return this.left;
    }

    public SortKeys getSortKeys() {
        return this.sortKeys;
    }

    public String getFileTypeExtension() {
        if (this.getLeft().equals(Material.NULL_OBJECT)) {
            return this.getRight().getIndexEntry().getFileType().getExtension();
        } else {
            return this.getLeft().getIndexEntry().getFileType().getExtension();
        }
    }

    public Material getRight() {
        return this.right;
    }

    public Material getDiff() {
        return this.diff;
    }

    public Double getDiffRatio() { return this.diffRatio; }

    public Boolean isChecked() { return this.checked; }

    public void setChecked(Boolean checked) {
        Objects.requireNonNull(checked);
        this.checked = checked;
    }

    public String getDiffRatioAsString() {
        return DifferUtil.formatDiffRatioAsString(this.getDiffRatio());
    }

    public JobTimestamp getReducedTimestamp() {
        return this.reducedTimestamp;
    }

    public QueryOnMetadata getQueryOnMetadata() {
        return this.query;
    }

    /**
     * String representation of this MaterialProduct instance
     *
     * @return
     */
    public String getDescription() {
        return this.query.getDescription(sortKeys);
    }

    @Override
    public String getId() {
        String json = this.toJson();
        return MaterialIO.hashJDK(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getShortId() {
        String id = this.getId();
        return id.substring(0, 7);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialProduct)) {
            return false;
        }
        MaterialProduct other = (MaterialProduct) obj;
        return this.getRight().equals(other.getRight()) && this.getLeft().equals(other.getLeft());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getLeft().hashCode();
        hash = 31 * hash + this.getRight().hashCode();
        if (this.getDiff() != null) {
            hash = 31 * hash + this.getDiff().hashCode();
        }

        return hash;
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public int compareTo(MaterialProduct other) {
        // Note that the SortKey is taken into account here indirectly
        return this.getDescription().compareTo(other.getDescription());
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"reducedTimestamp\":\"");
        sb.append(reducedTimestamp.toString());
        sb.append("\",");
        sb.append("\"checked\":");
        sb.append(checked);
        sb.append(",");
        sb.append("\"diffRatio\":");
        sb.append(diffRatio);
        sb.append(",");
        sb.append("\"fileTypeExtension\":");
        sb.append("\"" + getFileTypeExtension() + "\"");
        sb.append(",");
        sb.append("\"queryOnMetadata\":");
        sb.append(query.toJson());
        sb.append(",");
        sb.append("\"description\":");
        sb.append("\"" + JsonUtil.escapeAsJsonString(query.toJson()) + "\"");
        sb.append(",");
        sb.append("\"left\":");
        sb.append(left.toJson());
        sb.append(",");
        sb.append("\"right\":");
        sb.append(right.toJson());
        sb.append(",");
        sb.append("\"diff\":");
        sb.append(diff.toJson());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }

    }


    /**
     *
     */
    public static class Builder {
        private final Material left;
        private final Material right;
        private final JobTimestamp reducedTimestamp;
        private final Material diff;
        private QueryOnMetadata query;
        private final Double diffRatio;
        private SortKeys sortKeys;
        private final Boolean checked;
        public Builder(Material left, Material right, JobTimestamp reducedTimestamp) {
            Objects.requireNonNull(left);
            Objects.requireNonNull(right);
            Objects.requireNonNull(reducedTimestamp);
            this.left = left;
            this.right = right;
            this.reducedTimestamp = reducedTimestamp;
            this.diff = Material.NULL_OBJECT;
            this.query = QueryOnMetadata.NULL_OBJECT;
            this.diffRatio = 0.0d;
            this.sortKeys = SortKeys.NULL_OBJECT;
            this.checked = false;
        }

        public Builder setQueryOnMetadata(QueryOnMetadata query) {
            Objects.requireNonNull(query);
            this.query = query;
            return this;
        }

        public Builder sortKeys(SortKeys sortKeys) {
            this.sortKeys = sortKeys;
            return this;
        }

        public MaterialProduct build() {
            return new MaterialProduct(this);
        }
    }
}
