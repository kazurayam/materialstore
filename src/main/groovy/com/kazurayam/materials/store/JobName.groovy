package com.kazurayam.materials.store

class JobName implements Comparable {

    String jobName_

    JobName(String jobName) {
        if (!isValid(jobName)) {
            throw new IllegalArgumentException("${jobName} is not a valid JobName")
        }
        this.jobName_ = jobName
    }

    static boolean isValid(String s) {
        return Filename.isValid(s)
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof JobName) {
            return false
        }
        JobName other = (JobName)obj
        return this.jobName_ == other.jobName_
    }

    @Override
    int hashCode() {
        return jobName_.hashCode()
    }

    @Override
    String toString() {
        return jobName_
    }

    @Override
    int compareTo(Object o) {
        if (! o instanceof JobName) {
            throw new IllegalArgumentException("not instance of JobName")
        }
        JobName other = (JobName)o
        return this.jobName_ <=> other.jobName_
    }
}