package com.kazurayam.taod

class JobName implements Comparable {

    String jobName_

    JobName(String jobName) {
        validate(jobName)
        this.jobName_ = jobName
    }

    private void validate(String s) {
        if (! Filename.isValid(s)) {
            throw new IllegalArgumentException("${s} is not valid as a file/directory name")
        }
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
