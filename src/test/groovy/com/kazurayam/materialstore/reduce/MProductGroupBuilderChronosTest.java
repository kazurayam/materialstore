package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

public class MProductGroupBuilderChronosTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(MProductGroupBuilderChronosTest.class.getName());
    private static final Path fixtureDir = Paths.get(".").resolve("src/test/fixture/issue#80");
    private static final boolean verbose = true;
    private static Store store;
    private MaterialList left;
    private MaterialList right;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
        //
        if (verbose) {
            System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroup", "DEBUG");
        }

    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map).build());
        assert right.size() == 8;
        assert right.countMaterialsWithIdStartingWith("5d7e467") == 1;


        // modify the test fixture to meet the requirement for Chronos mode test
        // copy "20220128_191342" to "20220101_010101"; these 2 JobTimestamps makes a Chronos pair
        JobTimestamp timestampW = new JobTimestamp("20220101_010101");
        store.copyMaterials(jobName, timestampD, timestampW);

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        left = store.select(jobName, timestampW, QueryOnMetadata.builder(map1).build());
        assert left.size() == 8;
        assert left.countMaterialsWithIdStartingWith("5d7e467") == 1;
    }

    @Test
    public void test_chronos() throws MaterialstoreException {
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MProductGroup.builder(left,right)
                                .ignoreKeys("profile", "URL.host")
                                .identifyWithRegex(Collections.singletonMap("URL.query","\\w{32}"))
                                .build();
        MProductGroup reduced = MProductGroupBuilder.chronos(store, right, func);
        Assertions.assertNotNull(reduced);
        Assertions.assertEquals(1, reduced.getMaterialListPrevious().countMaterialsWithIdStartingWith("5d7e467"));
        Assertions.assertEquals(1, reduced.getMaterialListFollowing().countMaterialsWithIdStartingWith("5d7e467"));
        Assertions.assertEquals(8, reduced.size());
    }

}
