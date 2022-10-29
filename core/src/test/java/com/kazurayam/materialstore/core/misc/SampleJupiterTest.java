package com.kazurayam.materialstore.core.misc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of JUnit5 Test class based on https://www.baeldung.com/junit-5
 */
public class SampleJupiterTest {

    @BeforeAll
    public static void beforeAll() {
        logger.debug("@BeforeAll was called");
    }

    @BeforeEach
    public void setUp() {
        logger.debug("@BeforeEach was called");
    }

    @Test
    @Disabled("Not implemented yet")
    public void testShowSomething() {
    }

    @Test
    public void groupAssertions() {
        final Integer[] numbers = new Integer[]{0, 1, 2, 3, 4};
        Assertions.assertAll("numbers",
                () -> { Assertions.assertEquals(numbers[0], 0);},
                () -> { Assertions.assertEquals(numbers[3], 3);},
                () -> { Assertions.assertEquals(numbers[4], 4);}
        );
    }

    @Test
    public void trueAssumption() {
        Assumptions.assumeTrue(5 > 1);
        Assertions.assertEquals(5 + 2, 7);
    }

    @Test
    public void falseAssumption() {
        Assumptions.assumeFalse(5 < 1);
        Assertions.assertEquals(5 + 2, 7);
    }

    @Test
    public void assumingThat() {
        String someString = "Just a string";
        Assumptions.assumingThat(
                someString.equals("Just a string"),
                () -> { Assertions.assertEquals(2 + 2, 4);}
        );
    }

    @Test
    public void shouldThrowException() {
        Throwable exception =
                Assertions.assertThrows(UnsupportedOperationException.class, () -> {
                    throw new UnsupportedOperationException("Not supported");
                });
        Assertions.assertEquals(exception.getMessage(), "Not supported");
    }

    @Test
    public void assertThrowsException() {
        final String str = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Integer.valueOf(str);
        });
    }

    @AfterEach
    public void tearDown() {
        logger.debug("@AfterEach was called");
    }

    @AfterAll
    public static void afterAll() {
        logger.debug("@AfterAll was called");
    }

    private static Logger logger = LoggerFactory.getLogger(SampleJupiterTest.class);
}
