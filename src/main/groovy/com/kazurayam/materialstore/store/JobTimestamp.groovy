package com.kazurayam.materialstore.store

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalUnit

class JobTimestamp implements Comparable {

    public static final JobTimestamp NULL_OBJECT = new JobTimestamp("_")

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

    private String jobTimestamp_

    JobTimestamp(String jobTimestamp) {
        if (! isValid(jobTimestamp)) {
            throw new IllegalArgumentException("jobTimestamp(${jobTimestamp})" +
                    "must be in the format of ${formatter.toString()}")
        }
        this.jobTimestamp_ = jobTimestamp
    }

    JobTimestamp minus(long amountToSubtract, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, formatter)
        LocalDateTime calcLDT = base.minus(amountToSubtract, unit)
        String calcSTR = formatter.format(calcLDT)
        return new JobTimestamp(calcSTR)
    }

    JobTimestamp minusDays(long days) {
        return minus(days, ChronoUnit.DAYS)
    }

    JobTimestamp minusHours(long hours) {
        return minus(hours, ChronoUnit.HOURS)
    }

    JobTimestamp minusMinutes(long minutes) {
        return minus(minutes, ChronoUnit.MINUTES)
    }

    JobTimestamp minusMonths(long months) {
        return minus(months, ChronoUnit.MONTHS)
    }

    JobTimestamp minusSeconds(long seconds) {
        return minus(seconds, ChronoUnit.SECONDS)
    }

    JobTimestamp minusWeeks(long weeks) {
        return minus(weeks, ChronoUnit.WEEKS)
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
