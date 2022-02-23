package com.kazurayam.materialstore.resolvent

import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.differ.DifferUtil
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys

/**
 * Data Transfer Object
 */
final class Artifact implements Comparable {

    public static final Artifact NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                    JobTimestamp.NULL_OBJECT)
                    .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                    .build()

    private final Material left
    private final Material right
    private final JobTimestamp resolventTimestamp
    private final QueryOnMetadata query
    private final SortKeys sortKeys
    //
    private Material diff
    private Double diffRatio

    private Artifact(Builder builder) {
        this.left = builder.left
        this.right = builder.right
        this.diff = builder.diff
        this.resolventTimestamp = builder.resolventTimestamp
        this.query = builder.query
        this.diffRatio = 0.0d
        this.sortKeys = builder.sortKeys
    }

    /**
     * copy constructor
     *
     * @param source
     */
    Artifact(Artifact source) {
        Objects.requireNonNull(source)
        this.left = source.getLeft()
        this.right = source.getRight()
        this.diff = source.getDiff()
        this.resolventTimestamp = source.resolventTimestamp
        this.query = source.getQueryOnMetadata()
        this.sortKeys = source.getSortKeys()
    }

    void setDiff(Material diff) {
        Objects.requireNonNull(diff)
        this.diff = diff
    }

    void setDiffRatio(Double diffRatio) {
        Objects.requireNonNull(diffRatio)
        this.diffRatio = diffRatio
    }

    Material getLeft() {
        return this.left
    }

    SortKeys getSortKeys() {
        return this.sortKeys
    }

    String getFileTypeExtension() {
        if (this.getLeft() == Material.NULL_OBJECT) {
            return this.getRight().getIndexEntry().getFileType().getExtension()
        } else {
            return this.getLeft().getIndexEntry().getFileType().getExtension()
        }
    }

    Material getRight() {
        return this.right
    }

    Material getDiff() {
        return this.diff
    }

    Double getDiffRatio() {
        return this.diffRatio
    }

    String getDiffRatioAsString() {
        return DifferUtil.formatDiffRatioAsString(this.getDiffRatio())
    }

    JobTimestamp getResolventTimestamp() {
        return this.resolventTimestamp
    }

    QueryOnMetadata getQueryOnMetadata() {
        return this.query
    }

    /**
     * String representation of this Artifact instance
     *
     * @return
     */
    String getDescription() {
        return this.query.getDescription(sortKeys)
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Artifact) {
            return false
        }
        Artifact other = (Artifact)obj
        return this.getRight() == other.getRight() &&
                this.getLeft() == other.getLeft()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getLeft().hashCode()
        hash = 31 * hash + this.getRight().hashCode()
        if (this.getDiff() != null) {
            hash = 31 * hash + this.getDiff().hashCode()
        }
        return hash
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        // jobName is ignored as it is not necessary
        sb.append("\"left\":")
        sb.append(left.toString())
        sb.append(",")
        sb.append("\"right\":")
        sb.append(right.toString())
        sb.append(",")
        sb.append("\"resolventTimestamp\":\"")
        sb.append(resolventTimestamp.toString())
        sb.append("\",")
        sb.append("\"diff\":")
        sb.append(diff.toString())
        sb.append(",")
        sb.append("\"queryOnMetadata\":")
        sb.append(query.toString())
        sb.append(",")
        sb.append("\"diffRatio\":")
        sb.append(diffRatio)
        sb.append("}")
        return sb.toString()
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Artifact) {
            throw new IllegalArgumentException("obj is not instance of DiffResult")
        }
        Artifact other = (Artifact)obj

        // Note that the SortKey is taken into account here indirectly
        return this.getDescription() <=> other.getDescription()
    }

    /**
     *
     */
    static class Builder {
        // required
        private Material left
        private Material right
        private JobTimestamp resolventTimestamp
        // optional
        private Material diff
        private QueryOnMetadata query
        private Double diffRatio
        private SortKeys sortKeys
        Builder(Material left, Material right, JobTimestamp resolventTimestamp) {
            Objects.requireNonNull(left)
            Objects.requireNonNull(right)
            Objects.requireNonNull(resolventTimestamp)
            this.left = left
            this.right = right
            this.resolventTimestamp = resolventTimestamp
            this.diff = Material.NULL_OBJECT
            this.query = QueryOnMetadata.NULL_OBJECT
            this.diffRatio = -1.0d
            this.sortKeys = SortKeys.NULL_OBJECT
        }
        Builder setQueryOnMetadata(QueryOnMetadata query) {
            Objects.requireNonNull(query)
            this.query = query
            return this
        }
        Builder sortKeys(SortKeys sortKeys) {
            this.sortKeys = sortKeys
            return this
        }
        Artifact build() {
            return new Artifact(this)
        }
    }
}
