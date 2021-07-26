package com.kazurayam.materialstore.demo

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

/**
 * This test will pass only on the Mac Book Air of kazurayam.
 * This test dendes on the Mac Key Chain on that machine.
 * Nobody, other than kazurayam, would never be able to see this test passes.
 */
class MyKeyChainAccessorTest {

    @Test
    void test_findPassword_case1() {
        String password = new MyKeyChainAccessor().findPassword(
                "api.openweathermap.org", "kazurayam")
        assertNotNull(password)
        assertTrue(password.length() > 0)
        println "password: ${password}"
    }

    @Test
    void test_findPassword_case2() {
        String apiKey = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys", "Default")
        assertNotNull(apiKey)
        assertTrue(apiKey.length() > 0)
        println "Default: ${apiKey}"

    }

    @Test
    void test_findPassword_case3() {
        String apiKey = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys", "myFirstKey")
        assertNotNull(apiKey)
        assertTrue(apiKey.length() > 0)
        println "myFirstKey: ${apiKey}"

    }
}
