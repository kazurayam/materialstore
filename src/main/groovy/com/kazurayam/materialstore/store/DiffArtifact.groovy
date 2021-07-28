package com.kazurayam.materialstore.store

import com.kazurayam.materialstore.store.differ.DifferUtil
import groovy.json.JsonOutput

/**
 * Data Transfer Object
 */
class DiffArtifact implements Comparable {

    static final DiffArtifact NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT)
                    .descriptor(MetadataPattern.NULL_OBJECT)
                    .build()

    private final Material expected
    private final Material actual
    private final MetadataPattern descriptor
    //
    private Material diff
    private Double diffRatio

    private DiffArtifact(Builder builder) {
        this.expected = builder.expected
        this.actual = builder.actual
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
        this.expected = source.getExpected()
        this.actual = source.getActual()
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

    Material getExpected() {
        return this.expected
    }

    String getFileTypeExtension() {
        return this.getActual().getIndexEntry().getFileType().getExtension()
    }

    Material getActual() {
        return this.actual
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
        return this.getActual() == other.getActual() &&
                this.getExpected() == other.getExpected() &&
                this.getDiff() == other.getDiff()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getExpected().hashCode()
        hash = 31 * hash + this.getActual().hashCode()
        if (this.getDiff() != null) {
            hash = 31 * hash + this.getDiff().hashCode()
        }
        return hash
    }

    @Override
    String toString() {
        Map m = ["expected": expected.toString(),
                 "actual": actual.toString(),
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
        int comparisonOfExpected = this.getExpected() <=> other.getExpected()
        if (comparisonOfExpected == 0) {
            int comparisonOfActual = this.getActual() <=> other.getActual()
            if (comparisonOfActual == 0) {
                return this.getDiff() <=> other.getDiff()
            } else {
                return comparisonOfActual
            }
        } else {
            return comparisonOfExpected
        }
    }

    /**
     *
     */
    static class Builder {
        private Material expected
        private Material actual
        private MetadataPattern descriptor
        //
        private Material diff
        private Double diffRatio
        Builder(Material expected, Material actual) {
            Objects.requireNonNull(expected)
            Objects.requireNonNull(actual)
            this.expected = expected
            this.actual = actual
            this.descriptor = MetadataPattern.NULL_OBJECT
            this.diff = Material.NULL_OBJECT
            this.diffRatio = 0.0d
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
