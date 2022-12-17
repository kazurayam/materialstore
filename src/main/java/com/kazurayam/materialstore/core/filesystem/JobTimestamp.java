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

/**
 * JobTimestamp wraps an instance of java.time.LocalDateTime and
 * associates a fixed instance of java.time.format.DateTimeFormatter to be used.
 * The DateTimeFormat uses a format "uuuuMMdd_HHmmss".
 * The toString() method will return a String, for example, "20221129_110345".
 */
public final class JobTimestamp implements Comparable<JobTimestamp>, Jsonifiable {

    /**
     * special value that represents a NULL object.
     */
    public static final String EPOCH_NAME = "_";

    public static final JobTimestamp NULL_OBJECT = new JobTimestamp(EPOCH_NAME);

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss");

    private final String jobTimestamp_;

    /**
     * verifies if the given string is in the format of the FORMATTER, and returns true or false.
     * A under-bar "_" is accepted as the only exception, which stands for the case where
     * no concrete JobTimestamp is given.
     * @param s for example, "20221123_094521"
     * @return true if the s is in a valid format as JobTimestamp. otherwise false.
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
     * @param theTimestamp a JobTimestamp to copy
     * @return a new JobTimestamp with value of theTimestamp as LocalDateTime
     */
    public static JobTimestamp create(LocalDateTime theTimestamp) {
        return new JobTimestamp(FORMATTER.format(theTimestamp));
    }

    /**
     * @return a special instance of JobName of which value is a String "_" (under-bar).
     */
    public static JobTimestamp epoch() {
        return new JobTimestamp(EPOCH_NAME);
    }

    /**
     * @param timestamps a list of JobTimestamps to look at
     * @return the newest JobTimestamp value among the given timestamps
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
     * @param previous a list of JobTimestamps as comparison basis
     * @return a new JobTimestamp value which is assured to be newer than
     * any of previous JobTimestamps. If JobTimestamp.now() is newer than them, then
     * the value of JobTimestamp.now() will be returned.
     */
    public static JobTimestamp laterThan(JobTimestamp... previous) {
        return theTimeOrLaterThan(max(previous), now());
    }

    /**
     * When thanThis is "20221130_010101" and theTime is "20221130_010105", then a JobTimestamp of
     * "20221130_010105" will be returned.
     *
     * When thanThis is "20221130_010101" and theTime is "20221130_010100", then a JobTimestamp of
     * "20221130_010102", which is equal to (thanThis + 1 second), will be returned.
     *
     * @param thanThis a JobTimestamp as basis
     * @param theTime a JobTimestamp
     * @return a new JobTimestamp value which is assured to be newer thanThis at least for 1 second or more.
     *
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
     * calculate the time distance between following and previous in seconds.
     * @param previous e.g, 20221123_080000
     * @param following e.g, 20221123_080130
     * @return the seconds between the following minus the previous. E.g, (20221123_080130 - 20221123_080000) should return 90.
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
     * @param days number of days backward. E.g, 3 days.
     * @return a new JobTimestamp instance with the value which is "days"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusDays(long days) {
        return minus(days, ChronoUnit.DAYS);
    }

    /**
     * @param hours number of hours backward. E.g, 5 hours.
     * @return a new JobTimestamp instance with the value which is "hours"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusHours(long hours) {
        return minus(hours, ChronoUnit.HOURS);
    }

    /**
     * @param minutes number of minutes backward. E.g, 4 minutes.
     * @return a new JobTimestamp instance with the value which is "minutes"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusMinutes(long minutes) {
        return minus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * @param months nuber of months back. E.g, 2 months.
     * @return a new JobTimestamp instance with the value which is "months"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusMonths(long months) {
        return minus(months, ChronoUnit.MONTHS);
    }

    /**
     * @param seconds number of seconds back. E.g, 10 seconds.
     * @return a new JobTimestamp instance with the value which is "seconds"
     * before this JobTimestamp instance.
     */
    public JobTimestamp minusSeconds(long seconds) {
        return minus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * @param weeks number of weeks back. E.g, 3 weeks.
     * @return a new JobTimestamp instance with the value which is "weeks"
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
     * @param days number of days ahead. E.g, 2 days.
     * @return a new JobTimestamp instance with the value which is "days"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusDays(long days) {
        return plus(days, ChronoUnit.DAYS);
    }

    /**
     * @param hours number of hours ahead. E.g, 3 hours.
     * @return a new JobTimestamp instance with the value which is "hours"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusHours(long hours) {
        return plus(hours, ChronoUnit.HOURS);
    }

    /**
     * @param minutes number of minutes ahead. E.g, 2 minutes
     * @return a new JobTimestamp instance with the value which is "minutes"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusMinutes(long minutes) {
        return plus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * @param months number of months ahead. E.g, 2 months
     * @return a new JobTimestamp instance with the value which is "months"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusMonths(long months) {
        return plus(months, ChronoUnit.MONTHS);
    }

    /**
     * @param seconds number of seconds ahead. E.g, 5 seconds.
     * @return a new JobTimestamp instance with the value which is "seconds"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusSeconds(long seconds) {
        return plus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * @param weeks number of weeks ahead. E.g, 3 weeks
     * @return a new JobTimestamp instance with the value which is "weeks"
     * after this JobTimestamp instance.
     */
    public JobTimestamp plusWeeks(long weeks) {
        return plus(weeks, ChronoUnit.WEEKS);
    }

    /**
     *
     * @param second 0..59
     * @return a copy of this JobTimestamp instance but with the given second value
     */
    public JobTimestamp withSecond(int second) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.withSecond(second);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }

    /**
     *
     * @param minute 0..59
     * @return a copy of this JobTimestamp instance but with the given minute value
     */
    public JobTimestamp withMinute(int minute) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.withMinute(minute);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }

    /**
     *
     * @param hour 0..23
     * @return a copy of this JobTimestamp instance but with the given hour value
     */
    public JobTimestamp withHour(int hour) {
        LocalDateTime base = LocalDateTime.parse(this.jobTimestamp_, FORMATTER);
        LocalDateTime calcLDT = base.withHour(hour);
        String calcSTR = FORMATTER.format(calcLDT);
        return new JobTimestamp(calcSTR);
    }


    /**
     * @return a new JobTimestamp instance with the value which is the beginning of
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
     * @return a new JobTimestamp instance with the value which is the last day of
     * the month in which this JobTimestamp instance belongs. The hours will be 23, the minutes will be 59, the seconds will be 59.
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
     * If the value is the special "_", then LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC) will be returned
     * @return the LocalDateTime value of this instance
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
