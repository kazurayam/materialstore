package com.kazurayam.materials.store

import groovy.json.JsonOutput

class IndexEntry implements Comparable {

    private final ID id_
    private final FileType fileType_
    private final Metadata metadata_

    IndexEntry(ID id, FileType fileType, Metadata metadata) {
        this.id_ = id
        this.fileType_ = fileType
        this.metadata_ = metadata
    }

    ID getID() {
        return id_
    }

    FileType getFileType() {
        return fileType_
    }

    Metadata getMetadata() {
        return metadata_
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof IndexEntry) {
            return false
        }
        IndexEntry other = (IndexEntry)obj
        return this.getID() == other.getID() &&
                this.getFileType() == other.getFileType() &&
                this.getMetadata() == other.getMetadata()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getID().hashCode()
        hash = 31 * hash + this.getFileType().hashCode()
        hash = 31 * hash + this.getMetadata().hashCode()
        return hash
    }

    @Override
    String toString() {
        Map m = ["id": this.getID(), "fileType": this.getFileType(), "metadata": this.getMetadata()]
        return new JsonOutput().toJson(m)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof IndexEntry) {
            throw new IllegalArgumentException("obj is not an instance of IndexEntry")
        }
        IndexEntry other = (IndexEntry)obj
        int comparisonByMetadata = this.getMetadata() <=> other.getMetadata()
        if (comparisonByMetadata == 0) {
            int comparisonByFileType = this.getFileType() <=> other.getFileType()
            if (comparisonByFileType == 0) {
                return this.getID() <=> other.getID()
            } else {
                return comparisonByFileType
            }
        } else {
            return comparisonByMetadata
        }
    }
}

