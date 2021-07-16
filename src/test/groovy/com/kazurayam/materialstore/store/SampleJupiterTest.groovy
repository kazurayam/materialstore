package com.kazurayam.materialstore.store

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.junit.jupiter.api.Assertions.assertAll
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assumptions.assumeFalse
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

/**
 * An example of JUnit5 Test class based on https://www.baeldung.com/junit-5
 * modified to be runnable on Groovy 2.4.x
 */
class SampleJupiterTest {

    private static Logger logger = LoggerFactory.getLogger(SampleJupiterTest.class)

    @BeforeAll
    static void beforeAll() {
        logger.debug("@BeforeAll was called")
    }

    @BeforeEach
    void setUp()
    {
        logger.debug("@BeforeEach was called")
    }

    @Test
    @Disabled("Not implemented yet")
    void testShowSomething() {
    }

    /**
    @DisplayName("Java Lambda Expressions")
    @Test
    void lambdaExpressions() {
        assertTrue(Stream.of(1, 2, 3)
                .mapToInt (i -> Integer.valueOf(i) )
                .sum() > 5, () -> "Sum should be greater than 5")
    }
     */

    @Test
    void groupAssertions() {
        int[] numbers = [0, 1, 2, 3, 4] as int[];
        assertAll("numbers",
                        { -> assertEquals(numbers[0], 0) } as Executable,
                        { -> assertEquals(numbers[3], 3) } as Executable,
                        { -> assertEquals(numbers[4], 4) } as Executable
        )
    }

    @Test
    void trueAssumption() {
        assumeTrue(5 > 1);
        assertEquals(5 + 2, 7)
    }

    @Test
    void falseAssumption() {
        assumeFalse(5 < 1);
        assertEquals(5 + 2, 7)
    }

    @Test
    void assumptionThat() {
        String someString = "Just a string"
        assumingThat(
                someString.equals("Just a string"),
                { -> assertEquals(2 + 2, 4) }
        )
    }

    @Test
    void shouldThrowException() {
        Throwable exception = assertThrows(UnsupportedOperationException.class, { ->
            throw new UnsupportedOperationException("Not supported")
        })
        assertEquals(exception.getMessage(), "Not supported")
    }

    @Test
    void assertThrowsException() {
        String str = null
        assertThrows(IllegalArgumentException.class, { ->
            Integer.valueOf(str)
        })
    }

    @AfterEach
    public void tearDown(){
        logger.debug("@AfterEach was called")
    }

    @AfterAll
    public static void afterAll() {
        logger.debug("@AfterAll was called")
    }

}
