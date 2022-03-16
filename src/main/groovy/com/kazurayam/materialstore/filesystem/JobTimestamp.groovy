package com.kazurayam.materialstore.filesystem

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

final class JobTimestamp implements Comparable, Jsonifiable {

    public static final String EPOCH_NAME = "_"
    public static final JobTimestamp NULL_OBJECT = new JobTimestamp(EPOCH_NAME)
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss")

    static boolean isValid(String s) {
        if (s == EPOCH_NAME) {
            return true
        }
        try {
            FORMATTER.parse(s)
            return true
        } catch (DateTimeParseException e) {
            return false
        }
    }

    static JobTimestamp now() {
        LocalDateTime now = LocalDateTime.now()
        return new JobTimestamp(FORMATTER.format(now))
    }

    static JobTimestamp epoch() {
        return new JobTimestamp(EPOCH_NAME)
    }

    static JobTimestamp max(JobTimestamp ... timestamps) {
        JobTimestamp work = epoch()
        timestamps.each {jt ->
            if (jt > work) {
                work = jt
            }
        }
        return work
    }

    static JobTimestamp laterThan(JobTimestamp ... previous) {
        return theTimeOrLaterThan(max(previous), now())
    }

    static JobTimestamp theTimeOrLaterThan(JobTimestamp thanThis, JobTimestamp theTime) {
        long between = betweenSeconds(thanThis, theTime)
        if (between > 0) {
            return theTime
        } else {
            return max(thanThis, theTime).plusSeconds(1L)
        }
    }

    static long betweenSeconds(JobTimestamp previous, JobTimestamp following) {
        LocalDateTime previousLDT = previous.value()
        LocalDateTime followingLDT = following.value()
        return ChronoUnit.SECONDS.between(previousLDT, followingLDT)
    }

    private String jobTimestamp_

    /**
     * Sole constructor
     * @param jobTimestamp yyyymmdd_hhMMss e.g. "20210718_091328"; or "_"
     */
    JobTimestamp(String jobTimestamp) {
        if (! isValid(jobTimestamp)) {
            throw new IllegalArgumentException("jobTimestamp(${jobTimestamp}) must be in the format of ${FORMATTER.toString()}")
        }
        this.jobTimestamp_ = jobTimestamp
    }

    JobTimestamp minus(long amountToSubtract, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER)
        LocalDateTime calcLDT = base.minus(amountToSubtract, unit)
        String calcSTR = FORMATTER.format(calcLDT)
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

    JobTimestamp plus(long amountToAdd, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER)
        LocalDateTime calcLDT = base.plus(amountToAdd, unit)
        String calcSTR = FORMATTER.format(calcLDT)
        return new JobTimestamp(calcSTR)
    }

    JobTimestamp plusDays(long days) {
        return plus(days, ChronoUnit.DAYS)
    }

    JobTimestamp plusHours(long hours) {
        return plus(hours, ChronoUnit.HOURS)
    }

    JobTimestamp plusMinutes(long minutes) {
        return plus(minutes, ChronoUnit.MINUTES)
    }

    JobTimestamp plusMonths(long months) {
        return plus(months, ChronoUnit.MONTHS)
    }

    JobTimestamp plusSeconds(long seconds) {
        return plus(seconds, ChronoUnit.SECONDS)
    }

    JobTimestamp plusWeeks(long weeks) {
        return plus(weeks, ChronoUnit.WEEKS)
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
    String toJson() {
        return "\"" + jobTimestamp_ + "\""
    }

    @Override
    String toJson(boolean prettyPrint) {
        return toJson()
    }

    LocalDateTime value() {
        if (jobTimestamp_ == EPOCH_NAME) {
            return LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)
        } else {
            return LocalDateTime.parse(jobTimestamp_, FORMATTER)
        }
    }

    @Override
    int compareTo(Object o) {
        if (! o instanceof JobTimestamp) {
            throw new IllegalArgumentException("not instance of JobTimestamp")
        }
        JobTimestamp other = (JobTimestamp)o
        return this.value() <=> other.value()
    }
}
