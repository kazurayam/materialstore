package com.kazurayam.materialstore.demo

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class MacKeyChainWrapperTest {

    @Test
    void test_findPassword() {
        Map<String, String> cred = MacKeyChainWrapper.loadCredential("OpenWeatherMap")
        String password = MacKeyChainWrapper.findPassword(cred["server"], cred["account"])
        assertNotNull(password)
        assertTrue(password.length() > 0)
    }
}
