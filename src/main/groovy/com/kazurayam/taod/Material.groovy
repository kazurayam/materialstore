package com.kazurayam.taod

import groovy.json.JsonOutput

/**
 * This is a sort of Data Transfer Object that carries ProductObject
 * associated with a Metadata.
 *
 * A JobResult object implements a set of query methods that return
 * List<Product>
 */
class Material implements Comparable {

    private final Metadata metadata_
    private final MObject mObject_

    Material(Metadata metadata, MObject mObject) {
        this.metadata_ = metadata
        this.mObject_ = mObject
    }

    Metadata getMetadata() {
        return metadata_
    }

    MObject getMObject() {
        return mObject_
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Material) {
            return false
        }
        Material other = (Material)obj
        return this.getMetadata() == other.getMetadata() &&
                this.getMObject() == other.getMObject()
    }

    @Override
    int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.getMetadata().hashCode()
        hash = 31 * hash + this.getMObject().hashCode()
        return hash;
    }

    @Override
    String toString() {
        Map m = ["metadata": this.getMetadata(), "mObject": this.getMObject()]
        return new JsonOutput().toJson(m)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Material) {
            throw new IllegalArgumentException("obj is not instance of Material")
        }
        Material other = (Material)obj
        int metadataComparison = this.getMetadata() <=> other.getMetadata()
        if (metadataComparison == 0) {
            return this.getMObject() <=> other.getMObject()
        } else {
            return metadataComparison
        }
    }
}
