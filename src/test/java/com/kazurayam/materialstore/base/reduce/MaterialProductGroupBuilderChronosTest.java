package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaterialProductGroupBuilderChronosTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(MaterialProductGroupBuilderChronosTest.class);
    private static final Path fixtureDir = too.getProjectDir().resolve("src/test/fixtures/issue#80");
    private static final boolean verbose = true;
    private static Store store;
    private static MaterialList left;
    private static MaterialList right;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        too.cleanClassOutputDirectory();
        Path storePath = too.getClassOutputDirectory().resolve("store");
        too.copyDir(fixtureDir, storePath);
        store = Stores.newInstance(storePath);
        //
        if (verbose) {
            //System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.reduce.MProductGroup", "DEBUG");
        }

        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map).build());
        assert right.size() == 8;
        assert right.countMaterialsWithIdStartingWith("5d7e467") == 1;

        // modify the test fixture to meet the requirement for Chronos mode test
        // copy "20220128_191342" to "20211231_010101"; these 2 JobTimestamps makes a Chronos pair
        JobTimestamp timestampW = new JobTimestamp("20211231_010101");
        store.copyMaterials(jobName, timestampD, timestampW);

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "MyAdmin_DevelopmentEnv");
        left = store.select(jobName, timestampW, QueryOnMetadata.builder(map1).build());
        assert left.size() == 8;
        assert left.countMaterialsWithIdStartingWith("5d7e467") == 1;
    }

    @Test
    public void test_chronos_with_func() throws MaterialstoreException, JobNameNotFoundException {
        BiFunction<MaterialList, MaterialList, MaterialProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MaterialProductGroup.builder(left,right)
                                .ignoreKeys("environment", "URL.host")
                                .identifyWithRegex(Collections.singletonMap("URL.query","\\w{32}"))
                                .build();
        MaterialProductGroup reduced = Reducer.chronos(store, right, func);
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
    public void test_chronos_without_func()
            throws MaterialstoreException, JobNameNotFoundException {
        MaterialProductGroup reduced = Reducer.chronos(store, right);
        Assertions.assertNotNull(reduced);
        assertEquals(1, reduced.getMaterialListPrevious().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(1, reduced.getMaterialListFollowing().countMaterialsWithIdStartingWith("5d7e467"));
        assertEquals(8, reduced.size());
    }

    @Test
    public void test_chronos_priorTo_endOfLastMonth() throws MaterialstoreException, JobNameNotFoundException {
        JobTimestamp priorTo = right.getJobTimestamp().beginningOfTheMonth();
        assertEquals(new JobTimestamp("20220101_000000"), priorTo);
        MaterialProductGroup reduced = Reducer.chronos(store, right, priorTo);
        Assertions.assertNotNull(reduced);
        assertEquals(new JobTimestamp("20211231_010101"), reduced.getJobTimestampLeft());
    }
}
