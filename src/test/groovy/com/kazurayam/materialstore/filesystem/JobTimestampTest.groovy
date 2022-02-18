package com.kazurayam.materialstore.filesystem

import com.kazurayam.materialstore.filesystem.JobTimestamp
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class JobTimestampTest {

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
}
