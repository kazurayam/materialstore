package com.kazurayam.materialstore.filesystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;

public final class JobTimestamp implements Comparable<JobTimestamp>, Jsonifiable {

    public static final String EPOCH_NAME = "_";
    public static final JobTimestamp NULL_OBJECT = new JobTimestamp(EPOCH_NAME);
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss");
    private final String jobTimestamp_;

    public static boolean isValid(String s) {
        if (s.equals(EPOCH_NAME)) {
            return true;
        }

        try {
            FORMATTER.parse(s);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }

    }

    public static JobTimestamp now() {
        LocalDateTime now = LocalDateTime.now();
        return new JobTimestamp(FORMATTER.format(now));
    }

    public static JobTimestamp create(LocalDateTime theTimestamp) {
        return new JobTimestamp(FORMATTER.format(theTimestamp));
    }

    public static JobTimestamp epoch() {
        return new JobTimestamp(EPOCH_NAME);
    }

    public static JobTimestamp max(JobTimestamp... timestamps) {
        JobTimestamp work = epoch();
        for (JobTimestamp jt : timestamps) {
            long between = betweenSeconds(work, jt);
            if (between > 0) {
                work = jt;
            }
        }
        return work;
    }

    public static JobTimestamp laterThan(JobTimestamp... previous) {
        return theTimeOrLaterThan(max(previous), now());
    }

    public static JobTimestamp theTimeOrLaterThan(JobTimestamp thanThis, JobTimestamp theTime) {
        long between = betweenSeconds(thanThis, theTime);
        if (between > 0) {
            return theTime;
        } else {
            return max(thanThis, theTime).plusSeconds(1L);
        }

    }

    public static long betweenSeconds(JobTimestamp previous, JobTimestamp following) {
        LocalDateTime previousLDT = previous.value();
        LocalDateTime followingLDT = following.value();
        return ChronoUnit.SECONDS.between(previousLDT, followingLDT);
    }

    /**
     * Sole constructor
     *
     * @param jobTimestamp yyyymmdd_hhMMss e.g. "20210718_091328"; or "_"
     */
    public JobTimestamp(final String jobTimestamp) {
        if (!isValid(jobTimestamp)) {
            throw new IllegalArgumentException("jobTimestamp(" + jobTimestamp + ") must be in the format of " + FORMATTER.toString());
        }

        this.jobTimestamp_ = jobTimestamp;
    }

    public JobTimestamp minus(long amountToSubtract, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.minus(amountToSubtract, unit);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }

    public JobTimestamp minusDays(long days) {
        return minus(days, ChronoUnit.DAYS);
    }

    public JobTimestamp minusHours(long hours) {
        return minus(hours, ChronoUnit.HOURS);
    }

    public JobTimestamp minusMinutes(long minutes) {
        return minus(minutes, ChronoUnit.MINUTES);
    }

    public JobTimestamp minusMonths(long months) {
        return minus(months, ChronoUnit.MONTHS);
    }

    public JobTimestamp minusSeconds(long seconds) {
        return minus(seconds, ChronoUnit.SECONDS);
    }

    public JobTimestamp minusWeeks(long weeks) {
        return minus(weeks, ChronoUnit.WEEKS);
    }

    public JobTimestamp plus(long amountToAdd, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.plus(amountToAdd, unit);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }

    public JobTimestamp plusDays(long days) {
        return plus(days, ChronoUnit.DAYS);
    }

    public JobTimestamp plusHours(long hours) {
        return plus(hours, ChronoUnit.HOURS);
    }

    public JobTimestamp plusMinutes(long minutes) {
        return plus(minutes, ChronoUnit.MINUTES);
    }

    public JobTimestamp plusMonths(long months) {
        return plus(months, ChronoUnit.MONTHS);
    }

    public JobTimestamp plusSeconds(long seconds) {
        return plus(seconds, ChronoUnit.SECONDS);
    }

    public JobTimestamp plusWeeks(long weeks) {
        return plus(weeks, ChronoUnit.WEEKS);
    }

    public JobTimestamp beginningOfTheMonth() {
        LocalDate thisDay = this.value().toLocalDate();
        LocalDate theFirstDayOfTheMonth = thisDay.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime beginningOfTheMonth = LocalDateTime.of(theFirstDayOfTheMonth, LocalTime.MIDNIGHT);
        return JobTimestamp.create(beginningOfTheMonth);
    }

    public JobTimestamp endOfTheMonth() {
        LocalDate thisDay = this.value().toLocalDate();
        LocalDate theLastDayOfTheMonth = thisDay.with(TemporalAdjusters.lastDayOfMonth());
        LocalTime lt235959 = LocalTime.MIDNIGHT.minusSeconds(1);
        LocalDateTime endOfTheMonth = LocalDateTime.of(theLastDayOfTheMonth, lt235959);
        return JobTimestamp.create(endOfTheMonth);
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof JobTimestamp)) {
            return false;
        }
        JobTimestamp other = (JobTimestamp) obj;
        return this.jobTimestamp_.equals(other.jobTimestamp_);
    }

    @Override
    public int hashCode() {
        return jobTimestamp_.hashCode();
    }

    @Override
    public String toString() {
        return jobTimestamp_;
    }

    @Override
    public String toJson() {
        return "\"" + jobTimestamp_ + "\"";
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson();
    }

    public LocalDateTime value() {
        if (jobTimestamp_.equals(EPOCH_NAME)) {
            return LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC);
        } else {
            return LocalDateTime.parse(jobTimestamp_, FORMATTER);
        }

    }

    @Override
    public int compareTo(JobTimestamp other) {
        return this.value().compareTo(other.value());
    }


}
