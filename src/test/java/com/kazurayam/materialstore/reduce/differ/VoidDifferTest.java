package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.DifferDriver;
import com.kazurayam.materialstore.reduce.MProductGroup;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class VoidDifferTest {

    private static Store store;
    private JobName jobName;
    private MProductGroup prepared;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = projectDir.resolve("build/tmp/testOutput").resolve(VoidDifferTest.class.getName());
        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        Files.createDirectories(outputDir);
        Path fixtureDir = projectDir.resolve("src/test/fixture");
        FileUtils.copyDirectory(fixtureDir.resolve("issue#80").toFile(), outputDir.resolve("store").toFile());
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestamp1 = new JobTimestamp("20220128_191320");
        JobTimestamp timestamp2 = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.path", "/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2");
        MaterialList left = store.select(jobName, timestamp1, FileType.WOFF2, QueryOnMetadata.builder(map).build());
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("URL.path", "/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2");
        MaterialList right = store.select(jobName, timestamp2, FileType.WOFF2, QueryOnMetadata.builder(map1).build());
        prepared = MProductGroup.builder(left, right).ignoreKeys("profile", "URL.query").build();

        Assertions.assertNotNull(prepared);
        Assertions.assertEquals(1, prepared.size());
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        VoidDiffer voidDiffer = new VoidDiffer(store);
        DifferDriver differDriver = new DifferDriver.Builder(store).differFor(FileType.WOFF2, voidDiffer).build();
        differDriver.reduce(prepared);
        //
        JobTimestamp latestTimestamp = store.findLatestJobTimestamp(jobName);
        MaterialList materialList = store.select(jobName, latestTimestamp, QueryOnMetadata.ANY);
        Assertions.assertEquals(1, materialList.size());
        //
        Material material = materialList.get(0);
        Assertions.assertEquals(FileType.HTML, material.getIndexEntry().getFileType());
    }
}
