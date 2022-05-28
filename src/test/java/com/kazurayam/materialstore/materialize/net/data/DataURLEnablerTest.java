package com.kazurayam.materialstore.materialize.net.data;

import org.junit.jupiter.api.Test;

public class DataURLEnablerTest {

    @Test
    public void test_enableDataURL() {
        DataURLEnabler.enableDataURL();
        String value = System.getProperty(DataURLEnabler.PROPNAME);
        System.out.println(DataURLEnabler.PROPNAME + "=" + value);
    }
}
