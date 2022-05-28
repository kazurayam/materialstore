package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.dot.MPGVisualizer;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
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

public class MProductGroupBuilderTwinsTest {

    private static final Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MProductGroupBuilderTwinsTest.class.getName());
    private static final Path fixtureDir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#80");
    private static Store store;
    private JobName jobName;
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
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
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
    }

    @Test
    public void test_twins() {
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MProductGroup.builder(left, right)
                                .ignoreKeys("profile", "URL.host")
                                .identifyWithRegex(Collections.singletonMap("URL.query", "\\w{32}"))
                                .build();
        MProductGroup reduced = Reducer.twins(store, left, right, func);
        Assertions.assertNotNull(reduced);
        Assertions.assertEquals(8, reduced.size());
        Assertions.assertEquals(0, reduced.getNumberOfBachelors());
        //println JsonOutput.prettyPrint(reduced.toString())
    }

    @Test
    public void test_Bachelors() throws MaterialstoreException {
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MProductGroup.builder(left, right)
                                .ignoreKeys("profile", "URL.host")
                                //.identifyWithRegex(Collections.singletonMap("URL.query", "\\w{32}"))
                                .build();
        MProductGroup reduced = Reducer.twins(store, left, right, func);
        Assertions.assertNotNull(reduced);
        Assertions.assertEquals(9, reduced.size());
        Assertions.assertEquals(2, reduced.getNumberOfBachelors());
        //
        MPGVisualizer visualizer = new MPGVisualizer(store);
        JobName jobName = new JobName("test_Bachelors");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        visualizer.visualize(jobName, jobTimestamp, reduced);
    }

}
