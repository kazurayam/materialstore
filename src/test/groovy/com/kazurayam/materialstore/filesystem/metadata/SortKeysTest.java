package com.kazurayam.materialstore.filesystem.metadata;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class SortKeysTest {

    private static final Path fixtureDir = Paths.get(".").resolve("src/test/fixture/issue#89");
    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(SortKeysTest.class.getName());
    private static Store store;
    private MaterialList left;
    private MaterialList right;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            ResourceGroovyMethods.deleteDir(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        JobName jobName = new JobName("Flaskr_VisualInspectionTwins");
        JobTimestamp timestampP = new JobTimestamp("20220217_103054");
        JobTimestamp timestampD = new JobTimestamp("20220217_103106");

        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("profile", "Flaskr_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() == 14;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("profile", "Flaskr_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() == 14;
    }

    @Test
    public void test_smoke() {
        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL.host", "URL.port").sort("step", "URL.path").build();
        Assertions.assertEquals(14, mProductGroup.size());
        DefaultGroovyMethods.each(mProductGroup, new Closure<Object>(this, this) {
            public void doCall(Object it) {
                DefaultGroovyMethods.println(SortKeysTest.this, ((MaterialProduct) it).getDescription());
            }

        });
        Assertions.assertTrue(mProductGroup.get(0).getDescription().startsWith("{\"step\":\"1\""));
        Assertions.assertTrue(mProductGroup.get(1).getDescription().startsWith("{\"step\":\"1\""));
        Assertions.assertTrue(mProductGroup.get(2).getDescription().startsWith("{\"step\":\"2\""));
        Assertions.assertTrue(mProductGroup.get(3).getDescription().startsWith("{\"step\":\"2\""));
        Assertions.assertTrue(mProductGroup.get(4).getDescription().startsWith("{\"step\":\"3\""));
        Assertions.assertTrue(mProductGroup.get(5).getDescription().startsWith("{\"step\":\"3\""));
    }

    @Test
    public void test_constructor() {
        Assertions.assertNotNull(new SortKeys("step", "URL.path"));
    }

    @Test
    public void test_toString() {
        String json = new SortKeys("step", "URL.path").toString();
        DefaultGroovyMethods.println(this, json);
    }

}
