package com.kazurayam.taod

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class JobTimestamp implements Comparable {

    String jobTimestamp_

    static private DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_kkmmss")

    JobTimestamp(String jobTimestamp) {
        if (! validFormat(jobTimestamp)) {
            throw new IllegalArgumentException("jobTimestamp(${jobTimestamp})" +
                    "must be in the format of ${formatter.toString()}")
        }
        this.jobTimestamp_ = jobTimestamp
    }

    static boolean validFormat(String s) {
        try {
            formatter.parse(s)
            return true
        } catch (DateTimeParseException e) {
            return false
        }
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof JobTimestamp) {
            return false
        }
        JobTimestamp other = (JobTimestamp)obj
        return this.jobTimestamp_ == other.jobTimestamp_
    }

    @Override
    int hashCode() {
        return jobTimestamp_.hashCode()
    }

    @Override
    String toString() {
        return jobTimestamp_
    }

    @Override
    int compareTo(Object o) {
        if (! o instanceof JobTimestamp) {
            throw new IllegalArgumentException("not instance of JobTimestamp")
        }
        JobTimestamp other = (JobTimestamp)o
        return this.jobTimestamp_ <=> other.jobTimestamp_
    }
}
