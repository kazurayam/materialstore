package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.JobTimestamp
import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.assertNotEquals


class JobTimestampTest {

    private static final Pattern pattern = Pattern.compile("\\d{8}_\\d{6}")

    @Test
    void test_value_usual_case() {
        JobTimestamp now = JobTimestamp.now()
        LocalDateTime value = now.value()
        validate(value)
    }

    void validate(LocalDateTime value) {
        String formatted = JobTimestamp.FORMATTER.format(value)
        //println formatted
        assertTrue(pattern.matcher(formatted).matches(),
                formatted + " does not match " + pattern.toString())
    }

    @Test
    void test_value_special_case() {
        JobTimestamp special = new JobTimestamp("_")
        LocalDateTime value = special.value()
        validate(value)
    }

    @Test
    void test_betweenSeconds() {
        JobTimestamp previous  = new JobTimestamp("20220220_092830")
        JobTimestamp following = new JobTimestamp("20220220_092835")
        long between = JobTimestamp.betweenSeconds(previous, following)
        assertEquals(5L, between)
    }

    @Test
    void test_minusDays() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210726_223330")
        JobTimestamp previous = base.minusDays(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_minusHours() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210727_213330")
        JobTimestamp previous = base.minusHours(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_minusMinutes() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210727_223230")
        JobTimestamp previous = base.minusMinutes(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_minusMonths() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210627_223330")
        JobTimestamp previous = base.minusMonths(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_minusSeconds() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210727_223329")
        JobTimestamp previous = base.minusSeconds(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_minusWeeks() {
        JobTimestamp base     = new JobTimestamp("20210727_223330")
        JobTimestamp expected = new JobTimestamp("20210720_223330")
        JobTimestamp previous = base.minusWeeks(1)
        assertEquals(expected, previous)
    }

    @Test
    void test_plusDays() {
        JobTimestamp base     = new JobTimestamp("20210726_223330")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusDays(1)
        assertEquals(expected, following)
    }

    @Test
    void test_plusHours() {
        JobTimestamp base     = new JobTimestamp("20210727_213330")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusHours(1)
        assertEquals(expected, following)
    }

    @Test
    void test_plusMinutes() {
        JobTimestamp base     = new JobTimestamp("20210727_223230")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusMinutes(1)
        assertEquals(expected, following)
    }

    @Test
    void test_plusMonths() {
        JobTimestamp base     = new JobTimestamp("20210627_223330")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusMonths(1)
        assertEquals(expected, following)
    }

    @Test
    void test_plusSeconds() {
        JobTimestamp base     = new JobTimestamp("20210727_223329")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusSeconds(1)
        assertEquals(expected, following)
    }

    @Test
    void test_plusWeeks() {
        JobTimestamp base     = new JobTimestamp("20210720_223330")
        JobTimestamp expected = new JobTimestamp("20210727_223330")
        JobTimestamp following = base.plusWeeks(1)
        assertEquals(expected, following)
    }

    @Test
    void test_nowOrFollowing() {
        JobTimestamp previous = JobTimestamp.now()
        JobTimestamp actual = JobTimestamp.nowOrFollowing(previous)
        assertNotEquals(previous, actual)
        assertEquals(1L, JobTimestamp.betweenSeconds(previous, actual))
    }
}
