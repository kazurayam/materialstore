package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialProductGroup_BuilderTest {

    /*
    private static final Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(MaterialProductGroup_BuilderTest.class.getName());
    private static final Path fixtureDir =
            Paths.get(".").resolve("src/test/fixtures/issue#80");
    private static Store store;
    private JobName jobName;
    private MaterialList left;
    private MaterialList right;
*/

    @BeforeAll
    public static void beforeAll() throws IOException {
        /*
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
         */
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        /*
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() == 8;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() == 8;
         */
    }

    @Test
    public void test_ignoreKey_and_ignoreKeys() {
        MaterialList left = MaterialList.NULL_OBJECT;
        MaterialList right = MaterialList.NULL_OBJECT;
        MaterialProductGroup mpg = new MaterialProductGroup.Builder(left, right)
                .ignoreKey("profile")
                .ignoreKeys("URL.host", "URL.protocol")
                .ignoreKeys(Arrays.asList("URL.port", "URL.path"))
                .build();
        assertTrue(mpg.getIgnoreMetadataKeys().contains("profile"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.host"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.protocol"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.port"));
        assertTrue(mpg.getIgnoreMetadataKeys().contains("URL.path"));
        assertFalse(mpg.getIgnoreMetadataKeys().contains("hello"));
    }

}
