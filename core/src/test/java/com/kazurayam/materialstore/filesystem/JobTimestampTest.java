package com.kazurayam.materialstore.filesystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class JobTimestampTest {

    private static final Pattern pattern = Pattern.compile("\\d{8}_\\d{6}");

    @Test
    public void test_compareTo() {
        JobTimestamp epoch = new JobTimestamp("_");
        JobTimestamp previous = new JobTimestamp("20220220_092830");
        Assertions.assertTrue((epoch.compareTo(previous)) < 0);
        JobTimestamp following = new JobTimestamp("20220220_092835");
        Assertions.assertTrue((previous.compareTo(following)) < 0);
        Assertions.assertTrue((following.compareTo(epoch)) > 0);
        Assertions.assertTrue((epoch.compareTo(epoch)) == 0);
    }

    /**
     * 2022 April 8th, 8h 23m 16secs
     * should yield
     * 2022 March 31st, 23h 59m 59secs
     */
    @Test
    public void test_beginningOfTheMonth() {
        JobTimestamp currentTimestamp = new JobTimestamp("20220408_082316");
        JobTimestamp beginningOfTheMonth = currentTimestamp.beginningOfTheMonth();
        Assertions.assertEquals("20220401_000000", beginningOfTheMonth.toString());
    }

    @Test
    public void test_endOfTheMonth() {
        JobTimestamp currentTimestamp = new JobTimestamp("20220408_082316");
        JobTimestamp endOfTheMonth = currentTimestamp.endOfTheMonth();
        Assertions.assertEquals("20220430_235959", endOfTheMonth.toString());
    }


    @Test
    public void test_value_usual_case() {
        JobTimestamp now = JobTimestamp.now();
        LocalDateTime value = now.value();
        validate(value);
    }

    public static void validate(LocalDateTime value) {
        String formatted = JobTimestamp.FORMATTER.format(value);
        //println formatted
        Assertions.assertTrue(pattern.matcher(formatted).matches(), formatted + " does not match " + pattern.toString());
    }

    @Test
    public void test_value_special_case() {
        JobTimestamp special = new JobTimestamp("_");
        LocalDateTime value = special.value();
        validate(value);
    }

    @Test
    public void test_betweenSeconds() {
        JobTimestamp previous = new JobTimestamp("20220220_092830");
        JobTimestamp following = new JobTimestamp("20220220_092835");
        long between = JobTimestamp.betweenSeconds(previous, following);
        Assertions.assertEquals(5L, between);
    }

    @Test
    public void test_minusDays() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210726_223330");
        JobTimestamp previous = base.minusDays(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_minusHours() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210727_213330");
        JobTimestamp previous = base.minusHours(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_minusMinutes() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210727_223230");
        JobTimestamp previous = base.minusMinutes(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_minusMonths() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210627_223330");
        JobTimestamp previous = base.minusMonths(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_minusSeconds() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210727_223329");
        JobTimestamp previous = base.minusSeconds(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_minusWeeks() {
        JobTimestamp base = new JobTimestamp("20210727_223330");
        JobTimestamp expected = new JobTimestamp("20210720_223330");
        JobTimestamp previous = base.minusWeeks(1);
        Assertions.assertEquals(expected, previous);
    }

    @Test
    public void test_plusDays() {
        JobTimestamp base = new JobTimestamp("20210726_223330");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusDays(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_plusHours() {
        JobTimestamp base = new JobTimestamp("20210727_213330");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusHours(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_plusMinutes() {
        JobTimestamp base = new JobTimestamp("20210727_223230");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusMinutes(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_plusMonths() {
        JobTimestamp base = new JobTimestamp("20210627_223330");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusMonths(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_plusSeconds() {
        JobTimestamp base = new JobTimestamp("20210727_223329");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusSeconds(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_plusWeeks() {
        JobTimestamp base = new JobTimestamp("20210720_223330");
        JobTimestamp expected = new JobTimestamp("20210727_223330");
        JobTimestamp following = base.plusWeeks(1);
        Assertions.assertEquals(expected, following);
    }

    @Test
    public void test_laterThan() {
        JobTimestamp previous = JobTimestamp.now();
        // 1 argument
        JobTimestamp actual = JobTimestamp.laterThan(previous);
        Assertions.assertNotEquals(previous, actual);
        Assertions.assertEquals(1L, JobTimestamp.betweenSeconds(previous, actual));
        // 2 arguments
        JobTimestamp onceMore = JobTimestamp.laterThan(previous, actual);
        Assertions.assertNotEquals(previous, onceMore);
        Assertions.assertNotEquals(actual, onceMore);
    }

}
