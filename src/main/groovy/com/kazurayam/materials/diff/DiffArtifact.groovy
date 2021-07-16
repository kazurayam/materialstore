package com.kazurayam.materials.diff

import com.kazurayam.materials.store.Material
import com.kazurayam.materials.store.Metadata

/**
 * Data Transfer Object
 */
class DiffArtifact implements Comparable {

    private final Material expected
    private final Material actual
    private Material diff

    DiffArtifact(Material expected, Material actual) {
        Objects.requireNonNull(expected)
        Objects.requireNonNull(actual)
        this.expected = expected
        this.actual = actual
        this.diff = Material.NULL_OBJECT
    }

    void setDiff(Material diff) {
        Objects.requireNonNull(diff)
        this.diff = diff
    }

    Material getExpected() {
        return this.expected
    }

    Material getActual() {
        return this.actual
    }

    Material getDiff() {
        return this.diff
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
}
