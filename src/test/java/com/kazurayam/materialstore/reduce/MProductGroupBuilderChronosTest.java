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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MProductGroupBuilderChronosTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(MProductGroupBuilderChronosTest.class.getName());
    private static final Path fixtureDir = Paths.get(".").resolve("src/test/fixture/issue#80");
    private static final boolean verbose = true;
    private static Store store;
    private static MaterialList left;
    private static MaterialList right;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
        //
        if (verbose) {
            //System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroup", "DEBUG");
        }

        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map).build());
        assert right.size() == 8;
        assert right.countMaterialsWithIdStartingWith("5d7e467") == 1;

        // modify the test fixture to meet the requirement for Chronos mode test
        // copy "20220128_191342" to "20211231_010101"; these 2 JobTimestamps makes a Chronos pair
        JobTimestamp timestampW = new JobTimestamp("20211231_010101");
        store.copyMaterials(jobName, timestampD, timestampW);

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        left = store.select(jobName, timestampW, QueryOnMetadata.builder(map1).build());
        assert left.size() == 8;
        assert left.countMaterialsWithIdStartingWith("5d7e467") == 1;
    }

    @Test
    public void test_chronos_with_func() throws MaterialstoreException {
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MProductGroup.builder(left,right)
                                .ignoreKeys("profile", "URL.host")
                                .identifyWithRegex(Collections.singletonMap("URL.query","\\w{32}"))
                                .build();
        MProductGroup reduced = Reducer.chronos(store, right, func);
        Assertions.assertNotNull(reduced);
        assertEquals(1, reduced.getMaterialListPrevious().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(1, reduced.getMaterialListFollowing().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(8, reduced.size());

        // check how the MProductGroup is created:
        // the left Material and the right Material should be a valid Material
        // while the diff Material should be initial state
        Assertions.assertNotEquals(JobName.NULL_OBJECT, reduced.get(0).getLeft().getJobName());
        Assertions.assertNotEquals(JobName.NULL_OBJECT, reduced.get(0).getRight().getJobName());
        assertEquals(JobName.NULL_OBJECT, reduced.get(0).getDiff().getJobName());
        // System.out.println("reduce.get(0)=" + reduced.get(0).toJson(true));
    }

    @Test
    public void test_chronos_without_func() throws MaterialstoreException {
        MProductGroup reduced = Reducer.chronos(store, right);
        Assertions.assertNotNull(reduced);
        assertEquals(1, reduced.getMaterialListPrevious().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(1, reduced.getMaterialListFollowing().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(8, reduced.size());
    }

    @Test
    public void test_chronos_priorTo_endOfLastMonth() throws MaterialstoreException {
        JobTimestamp priorTo = right.getJobTimestamp().beginningOfTheMonth();
        assertEquals(new JobTimestamp("20220101_000000"), priorTo);
        MProductGroup reduced = Reducer.chronos(store, right, priorTo);
        Assertions.assertNotNull(reduced);
        assertEquals(new JobTimestamp("20211231_010101"), reduced.getJobTimestampLeft());
    }
}
