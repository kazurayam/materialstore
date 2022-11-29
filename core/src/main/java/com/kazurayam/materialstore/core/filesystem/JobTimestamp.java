package com.kazurayam.materialstore.core.filesystem;

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

    /**
     * verifies if the given string is in the format of the FORMATTER, and returns true or false.
     * A under-bar "_" is accepted as the only exception, which stands for the case where
     * no concrete JobTimestamp is given.
     */
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

    /**
     * @return an instance of JobTimestamp with the value of java.time.LocalDateTime.now()
     */
    public static JobTimestamp now() {
        LocalDateTime now = LocalDateTime.now();
        return new JobTimestamp(FORMATTER.format(now));
    }

    /**
     * create a new JobTimestamp with value of theTimestamp as LocalDateTime
     */
    public static JobTimestamp create(LocalDateTime theTimestamp) {
        return new JobTimestamp(FORMATTER.format(theTimestamp));
    }

    /**
     * create a special instance of JobName of which value is a String "_" (under-bar).
     */
    public static JobTimestamp epoch() {
        return new JobTimestamp(EPOCH_NAME);
    }

    /**
     * returns the newest JobTimestamp value among the given timestamps
     */
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

    /**
     * will return a new JobTimestamp value which is assured to be newer than
     * any of previous JobTimestamps. If JobTimestamp.now() is newer than them, then
     * the value of JobTimestamp.now() will be returned.
     */
    public static JobTimestamp laterThan(JobTimestamp... previous) {
        return theTimeOrLaterThan(max(previous), now());
    }

    /**
     * will return a new JobTimestamp value which is assured to be newer thanThis at least for 1 second or more.
     *
     * When thanThis is "20221130_010101" and theTime is "20221130_010105", then a JobTimestamp of
     * "20221130_010105" will be returned.
     *
     * When thanThis is "20221130_010101" and theTime is "20221130_010100", then a JobTimestamp of
     * "20221130_010102", which is equal to thanThis plus 1 second, will be returned.
     */
    public static JobTimestamp theTimeOrLaterThan(JobTimestamp thanThis, JobTimestamp theTime) {
        long between = betweenSeconds(thanThis, theTime);
        if (between > 0) {
            return theTime;
        } else {
            return max(thanThis, theTime).plusSeconds(1L);
        }

    }

    /**
     * @return return the seconds between the following minus the previous.
     */
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

    /**
     * return a new JobTimestamp instance with the value which is "days"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusDays(long days) {
        return minus(days, ChronoUnit.DAYS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "hours"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusHours(long hours) {
        return minus(hours, ChronoUnit.HOURS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "minutes"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusMinutes(long minutes) {
        return minus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * return a new JobTimestamp instance with the value which is "months"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusMonths(long months) {
        return minus(months, ChronoUnit.MONTHS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "seconds"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusSeconds(long seconds) {
        return minus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "weeks"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusWeeks(long weeks) {
        return minus(weeks, ChronoUnit.WEEKS);
    }

    public JobTimestamp plus(long amountToAdd, TemporalUnit unit) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.plus(amountToAdd, unit);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }

    /**
     * return a new JobTimestamp instance with the value which is "days"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusDays(long days) {
        return plus(days, ChronoUnit.DAYS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "hours"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusHours(long hours) {
        return plus(hours, ChronoUnit.HOURS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "minutes"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusMinutes(long minutes) {
        return plus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * return a new JobTimestamp instance with the value which is "months"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusMonths(long months) {
        return plus(months, ChronoUnit.MONTHS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "seconds"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusSeconds(long seconds) {
        return plus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * return a new JobTimestamp instance with the value which is "weeks"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusWeeks(long weeks) {
        return plus(weeks, ChronoUnit.WEEKS);
    }

    /**
     * return a new JobTimestamp instance with the value which is the beginning of
     * the month in which this JobTimestamp instance belongs.
     * The hours will be 00, the minutes will be 00, the seconds will be 00.
     */
    public JobTimestamp beginningOfTheMonth() {
        LocalDate thisDay = this.value().toLocalDate();
        LocalDate theFirstDayOfTheMonth = thisDay.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime beginningOfTheMonth = LocalDateTime.of(theFirstDayOfTheMonth, LocalTime.MIDNIGHT);
        return JobTimestamp.create(beginningOfTheMonth);
    }

    /**
     * return a new JobTimestamp instance with the value which is the last day of
     * the month in which this JobTimestamp instance belongs.
     * The hours will be 23, the minutes will be 59, the seconds will be 59.
     */
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

    /**
     * E.g, 20221129_195423 represents the year 2022, the month 11, the day 29,
     * the hours 19, the minutes 54, the second 23.
     */
    @Override
    public String toString() {
        return jobTimestamp_;
    }

    /**
     *
     * @return "20221129_195423" enclosed by double-quotes
     */
    @Override
    public String toJson() {
        return "\"" + jobTimestamp_ + "\"";
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson();
    }

    /**
     * will return the LocalDateTime value of this instance.
     * If the value is the special "_", then LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC) will be returned
     */
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
