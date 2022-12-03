package com.kazurayam.materialstore.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GsonHelperTest {

    @Test
    public void test_toStringStringMap() {
        Map<String, Object> m = new HashMap<>();
        Map<String, Integer> s = new HashMap<>();
        s.put("name", 123);
        m.put("test", s);
        Map<String, String> r = GsonHelper.toStringStringMap(s);
        System.out.println("r=" + r.toString());
    }
}
