package com.kazurayam.materials.store

import groovy.json.JsonOutput

class Material implements Comparable {

    static final Material NULL_OBJECT =
            new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, IndexEntry.NULL_OBJECT)

    private final JobName jobName_
    private final JobTimestamp jobTimestamp_
    private final IndexEntry indexEntry_

    Material(JobName jobName, JobTimestamp jobTimestamp, IndexEntry indexEntry) {
        this.jobName_ = jobName
        this.jobTimestamp_ = jobTimestamp
        this.indexEntry_ = indexEntry
    }

    JobName getJobName() {
        return jobName_
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp_
    }

    IndexEntry getIndexEntry() {
        return indexEntry_
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof Material) {
            return false
        }
        Material other = (Material)obj
        return this.getJobName() == other.getJobName() &&
                this.getJobTimestamp() == other.getJobTimestamp() &&
                this.getIndexEntry() == other.getIndexEntry()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getJobName().hashCode()
        hash = 31 * hash + this.getJobTimestamp().hashCode()
        hash = 31 * hash + this.getIndexEntry().hashCode()
        return hash
    }

    @Override
    String toString() {
        Map m = ["jobName": this.getJobName(), "jobTimestamp": this.getJobTimestamp(),
                 "indexEntry": this.getIndexEntry()]
        return new JsonOutput().toJson(m)
    }

    @Override
    int compareTo(Object obj) {
        if (! obj instanceof Material) {
            throw new IllegalArgumentException("obj is not an instance of Material")
        }
        Material other = (Material)obj
        int comparisonByJobName = this.getJobName() <=> other.getJobName()
        if (comparisonByJobName == 0) {
            int comparisonByJobTimestamp = this.getJobTimestamp() <=> other.getJobTimestamp()
            if (comparisonByJobTimestamp == 0) {
                return this.getIndexEntry() <=> other.getIndexEntry()
            } else {
                return comparisonByJobTimestamp
            }
        } else {
            return comparisonByJobName
        }
    }
}

