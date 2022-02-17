package com.kazurayam.materialstore


import com.kazurayam.materialstore.differ.DifferUtil

/**
 * Data Transfer Object
 */
final class DiffArtifact implements Comparable {

    public static final DiffArtifact NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT)
                    .setMetadataPattern(MetadataPattern.NULL_OBJECT)
                    .build()

    private final Material left
    private final Material right
    private final MetadataPattern metadataPattern
    private final DiffArtifactComparisonPriorities comparisonPriorities
    //
    private Material diff
    private Double diffRatio

    private DiffArtifact(Builder builder) {
        this.left = builder.left
        this.right = builder.right
        this.metadataPattern = builder.metadataPattern
        this.diff = Material.NULL_OBJECT
        this.diffRatio = 0.0d
        this.comparisonPriorities = builder.comparisonPriorities
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
        this.metadataPattern = source.getDescriptor()
        this.comparisonPriorities = source.getDiffArtifactComparisonPriorities()
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

    DiffArtifactComparisonPriorities getDiffArtifactComparisonPriorities() {
        return this.comparisonPriorities
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

    MetadataPattern getDescriptor() {
        return this.metadataPattern
    }

    String getDescription() {
        return this.metadataPattern.getDescription(comparisonPriorities)
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
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"left\":")
        sb.append(left.toString())
        sb.append(",")
        sb.append("\"right\":")
        sb.append(right.toString())
        sb.append(",")
        sb.append("\"diff\":")
        sb.append(diff.toString())
        sb.append(",")
        sb.append("\"metadataPattern\":")
        sb.append(metadataPattern.toString())
        sb.append(",")
        sb.append("\"diffRatio\":")
        sb.append(diffRatio)
        sb.append("}")
        return sb.toString()
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof DiffArtifact) {
            throw new IllegalArgumentException("obj is not instance of DiffResult")
        }
        DiffArtifact other = (DiffArtifact)obj

        // Note that the DiffArtifactComparisonPriorities is taken into account here
        return this.getDescription() <=> other.getDescription()
    }

    /**
     *
     */
    static class Builder {
        private Material left
        private Material right
        private MetadataPattern metadataPattern
        //
        private Material diff
        private Double diffRatio
        private DiffArtifactComparisonPriorities comparisonPriorities
        Builder(Material left, Material right) {
            Objects.requireNonNull(left)
            Objects.requireNonNull(right)
            this.left = left
            this.right = right
            this.metadataPattern = MetadataPattern.NULL_OBJECT
            this.diff = Material.NULL_OBJECT
            this.diffRatio = -1.0d
            this.comparisonPriorities = DiffArtifactComparisonPriorities.NULL_OBJECT
        }
        Builder setMetadataPattern(MetadataPattern metadataPattern) {
            Objects.requireNonNull(metadataPattern)
            this.metadataPattern = metadataPattern
            return this
        }
        Builder setDiffArtifactComparisonPriorities(DiffArtifactComparisonPriorities comparisonPriorities) {
            this.comparisonPriorities = comparisonPriorities
            return this
        }
        DiffArtifact build() {
            return new DiffArtifact(this)
        }
    }
}
