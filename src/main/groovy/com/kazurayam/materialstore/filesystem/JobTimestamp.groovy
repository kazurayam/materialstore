package com.kazurayam.materialstore.filesystem

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

final class JobTimestamp implements Comparable {

    public static final String SPECIAL_NAME = "_"
    public static final JobTimestamp NULL_OBJECT = new JobTimestamp(SPECIAL_NAME)
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_kkmmss")

    static boolean isValid(String s) {
        if (s == "_") {
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

    static JobTimestamp nowOrFollowing(JobTimestamp previous) {
        return theTimeOrFollowing(previous, now())
    }

    static JobTimestamp theTimeOrFollowing(JobTimestamp previous, JobTimestamp theTime) {
        long between = betweenSeconds(previous, theTime)
        if (between > 0) {
            return theTime
        } else {
            return theTime.plusSeconds(1L)
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
            throw new IllegalArgumentException("jobTimestamp(${jobTimestamp})" +
                    "must be in the format of ${FORMATTER.toString()}")
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

    LocalDateTime value() {
        if (jobTimestamp_ == SPECIAL_NAME) {
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
        return this.jobTimestamp_ <=> other.jobTimestamp_
    }
}
