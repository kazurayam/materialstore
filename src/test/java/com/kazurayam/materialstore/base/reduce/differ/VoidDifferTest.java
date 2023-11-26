package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.DiffingMPGProcessor;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
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

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(VoidDifferTest.class);
    private static Store store;
    private JobName jobName;
    private MaterialProductGroup reducedMPG;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path classOutputDir = too.getClassOutputDirectory();
        Path fixtureDir = too.getProjectDir().resolve("src/test/fixtures");
        too.copyDir(fixtureDir.resolve("issue#80"),
                classOutputDir.resolve("store"));
        Path root = classOutputDir.resolve("store");
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
        reducedMPG = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL.query").build();

        Assertions.assertNotNull(reducedMPG);
        Assertions.assertEquals(1, reducedMPG.size());
    }

    @Test
    public void test_smoke() throws MaterialstoreException, JobNameNotFoundException {
        VoidDiffer voidDiffer = new VoidDiffer(store);
        DiffingMPGProcessor differDriver = new DiffingMPGProcessor.Builder(store).differFor(FileType.WOFF2, voidDiffer).build();
        differDriver.process(reducedMPG);
        //
        JobTimestamp latestTimestamp = store.findLatestJobTimestamp(jobName);
        MaterialList materialList = store.select(jobName, latestTimestamp, QueryOnMetadata.ANY);
        Assertions.assertEquals(1, materialList.size());
        //
        Material material = materialList.get(0);
        Assertions.assertEquals(FileType.HTML, material.getIndexEntry().getFileType());
    }
}
