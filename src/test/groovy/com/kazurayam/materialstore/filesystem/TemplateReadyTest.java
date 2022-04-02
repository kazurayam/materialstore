package com.kazurayam.materialstore.filesystem;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateReadyTest {

    @Test
    public void test_toTemplateReady() {
        Foo foo = new Foo("key1", "value1");
        Map<String, Object> model = foo.toTemplateModel();
        assertTrue(model.containsKey("metadata"));
        Object metadata = model.get("metadata");
        assertNotNull(metadata);
    }


    private static class Foo implements TemplateReady {
        private String key;
        private String value;
        public Foo(String key, String value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public String toJson() {
            return "{\"metadata\":{\"" + key + "\":\"" + value + "\"}}";
        }
        @Override
        public String toJson(boolean prettyPrint) {
            return toJson();
        }
    }
}
