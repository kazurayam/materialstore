package com.kazurayam.materialstore

import com.kazurayam.materialstore.differ.DifferUtil
import groovy.json.JsonOutput

/**
 * Data Transfer Object
 */
final class DiffArtifact implements Comparable {

    public static final DiffArtifact NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT)
                    .descriptor(MetadataPattern.NULL_OBJECT)
                    .build()

    private final Material left
    private final Material right
    private final MetadataPattern descriptor
    //
    private Material diff
    private Double diffRatio

    private DiffArtifact(Builder builder) {
        this.left = builder.left
        this.right = builder.right
        this.descriptor = builder.descriptor
        this.diff = Material.NULL_OBJECT
        this.diffRatio = 0.0d
    }

    /**
     * copy constructor
     *
     * @param source
     */
    DiffArtifact(DiffArtifact source) {
        Objects.requireNonNull(source)
        this.left = source.getLeft()
        this.right = source.getRight()
        this.diff = source.getDiff()
        this.descriptor = source.getDescriptor()
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

    String getFileTypeExtension() {
        return this.getRight().getIndexEntry().getFileType().getExtension()
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

    MetadataPattern getDescriptor() {
        return this.descriptor
    }

    String getDescription() {
        return this.descriptor.toString()
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof DiffArtifact) {
            return false
        }
        DiffArtifact other = (DiffArtifact)obj
        return this.getRight() == other.getRight() &&
                this.getLeft() == other.getLeft() &&
                this.getDiff() == other.getDiff()
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
        Map m = ["left": left.toString(),
                 "right": right.toString(),
                 "diff": diff.toString(),
                 "descriptor": descriptor.toString(),
                 "diffRatio": diffRatio]
        return JsonOutput.toJson(m)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof DiffArtifact) {
            throw new IllegalArgumentException("obj is not instance of DiffResult")
        }
        DiffArtifact other = (DiffArtifact)obj
        int comparisonOfLeft = this.getLeft() <=> other.getLeft()
        if (comparisonOfLeft == 0) {
            int comparisonOfRight = this.getRight() <=> other.getRight()
            if (comparisonOfRight == 0) {
                return this.getDiff() <=> other.getDiff()
            } else {
                return comparisonOfRight
            }
        } else {
            return comparisonOfLeft
        }
    }

    /**
     *
     */
    static class Builder {
        private Material left
        private Material right
        private MetadataPattern descriptor
        //
        private Material diff
        private Double diffRatio
        Builder(Material left, Material right) {
            Objects.requireNonNull(left)
            Objects.requireNonNull(right)
            this.left = left
            this.right = right
            this.descriptor = MetadataPattern.NULL_OBJECT
            this.diff = Material.NULL_OBJECT
            this.diffRatio = -1.0d
        }
        Builder descriptor(MetadataPattern descriptor) {
            Objects.requireNonNull(descriptor)
            this.descriptor = descriptor
            return this
        }
        DiffArtifact build() {
            return new DiffArtifact(this)
        }
    }
}
