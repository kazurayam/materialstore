package com.kazurayam.materialstore.store

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class JobTimestamp implements Comparable {

    static final JobTimestamp NULL_OBJECT = new JobTimestamp("_")

    static private DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_kkmmss")

    static boolean isValid(String s) {
        if (s == "_") {
            return true
        }
        try {
            formatter.parse(s)
            return true
        } catch (DateTimeParseException e) {
            return false
        }
    }

    static JobTimestamp now() {
        LocalDateTime now = LocalDateTime.now()
        return new JobTimestamp(formatter.format(now))
    }

    String jobTimestamp_

    JobTimestamp(String jobTimestamp) {
        if (! isValid(jobTimestamp)) {
            throw new IllegalArgumentException("jobTimestamp(${jobTimestamp})" +
                    "must be in the format of ${formatter.toString()}")
        }
        this.jobTimestamp_ = jobTimestamp
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
