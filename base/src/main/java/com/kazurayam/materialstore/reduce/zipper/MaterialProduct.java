package com.kazurayam.materialstore.reduce.zipper;

import com.kazurayam.materialstore.filesystem.Identifiable;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialIO;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.QueryIdentification;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
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
    private Material diff;
    private Double diffRatio;

    private MaterialProduct(Builder builder) {
        this.left = builder.left;
        this.right = builder.right;
        this.diff = builder.diff;
        this.reducedTimestamp = builder.reducedTimestamp;
        this.query = builder.query;
        this.diffRatio = builder.diffRatio;
    }

    /**
     * copy constructor
     *
     */
    public MaterialProduct(MaterialProduct source) {
        Objects.requireNonNull(source);
        this.left = source.getLeft();
        this.right = source.getRight();
        this.diff = source.getDiff();
        this.reducedTimestamp = source.getReducedTimestamp();
        this.query = source.getQueryOnMetadata();
        this.diffRatio = source.getDiffRatio();
    }

    public void annotate(IgnoreMetadataKeys ignoreMetadataKeys,
                         IdentifyMetadataValues identifyMetadataValues) {
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

    /**
     * return true if either of the left Material or the right Material is
     * Material.NULL_OBJECT object. In other words, return true if this
     * MaterialProduct object has single Material object contained;
     * return false if both of the left and right is stuffed Material.
     *
     * @return true if either of the left Material or the right Material is NULL object
     */
    public Boolean isBachelor() {
        return (getLeft().isEmpty() || getRight().isEmpty());
    }

    public JobTimestamp getReducedTimestamp() {
        return this.reducedTimestamp;
    }

    public QueryOnMetadata getQueryOnMetadata() {
        return this.query;
    }

    public QueryIdentification getQueryIdentification() {
        return this.query.getQueryIdentification();
    }

    public QueryIdentification getQueryIdentification(SortKeys sortKeys) {
        return this.query.getQueryIdentification(sortKeys);
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
        return this.getQueryIdentification().compareTo(other.getQueryIdentification());
    }

    public boolean contains(Material material) {
        return this.containsMaterialAt(material) != 0;
    }

    public int containsMaterialAt(Material material) {
        if (this.left == material) {
            return -1;
        } else if (this.right == material) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        //sb.append("\"reducedTimestamp\":\"");
        //sb.append(reducedTimestamp.toString());
        //sb.append("\",");

        // The "checked" property is used by the JavaScript in the report HTML
        // for rendering "WIT".
        // see https://github.com/kazurayam/materialstore/issues/224 to know
        // what "WIT" is.
        sb.append("\"checked\":");
        sb.append("false");
        sb.append(",");

        sb.append("\"isBachelor\":");
        sb.append(this.isBachelor());
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
        }

        public Builder setQueryOnMetadata(QueryOnMetadata query) {
            Objects.requireNonNull(query);
            this.query = query;
            return this;
        }

        public MaterialProduct build() {
            return new MaterialProduct(this);
        }
    }
}
