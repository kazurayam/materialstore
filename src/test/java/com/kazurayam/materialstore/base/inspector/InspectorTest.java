package com.kazurayam.materialstore.base.inspector;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.TestHelper;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.ID;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InspectorTest {

    private static Store store;
    private MaterialList left;
    private MaterialList right;
    private Inspector inspector;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(InspectorTest.class);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
        Path issue80Dir = TestHelper.getFixturesDirectory().resolve("issue#80");
        FileUtils.copyDirectory(issue80Dir.toFile(), store.getRoot().toFile());
    }

    @BeforeEach
    public void before() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        inspector = Inspector.newInstance(store);
    }

    @Test
    public void test_reduceAndSort() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup reducedMPG =
                MaterialProductGroup.builder(left, right)
                        .ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol")
                        .identifyWithRegex(map)
                        .sort("URL.host")
                        .build();
        assertNotNull(reducedMPG);

        MaterialProductGroup processedMPG = inspector.reduceAndSort(reducedMPG);
        assertNotNull(processedMPG);

        processedMPG.forEach(mProduct -> Assertions.assertNotEquals(ID.NULL_OBJECT,
                mProduct.getDiff().getIndexEntry().getID()));
        assertEquals(8, processedMPG.size());
    }

    @Test
    public void test_report_MaterialList() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp jobTimestamp = new JobTimestamp("20220128_191320");
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        //System.out.println("materialList=" + materialList.toTemplateModel());
        Inspector inspector = Inspector.newInstance(store);
        Path report = inspector.report(materialList);
        assertNotNull(report);
        assertTrue(Files.exists(report));
    }

    @Test
    public void test_report_MaterialProductGroup() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right)
                .ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol")
                .identifyWithRegex(map)
                .sort("URL.host")
                .build();
        MaterialProductGroup reduced = inspector.reduceAndSort(mpg);
        double threshold = 0.0D;
        assertTrue(reduced.countWarnings(threshold) > 0);

        Path report = inspector.report(reduced, threshold);
        assertNotNull(report);
        assertTrue(Files.exists(report));
    }

    /*
     * https://github.com/kazurayam/materialstore/issues/393
     * Even when the left MaterialList is empty, Inspector should work quietly
     * and produce a report that shows "There isn't anything to compare"
     */
    @Test
    public void test_report_MaterialProductGroup_ThereIsntAnythingToCompare()
            throws MaterialstoreException {
        MaterialList emptyML =
                store.select(new JobName("MyAdmin_visual_inspection_twins"),
                        new JobTimestamp("20220128_191320"),
                        FileType.JPEG);   // I know, there is no JPEG
        //
        MaterialProductGroup mpg = MaterialProductGroup.builder(emptyML, right).build();
        MaterialProductGroup reduced = inspector.reduceAndSort(mpg);
        double threshold = 0.0D;
        assertTrue(reduced.countWarnings(threshold) > 0);
        Path report = inspector.report(reduced, threshold);
        assertNotNull(report);
        assertTrue(Files.exists(report));
    }

    @Test
    public void test_resolveReportFileName_MaterialList() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp jobTimestamp = new JobTimestamp("20220128_191320");
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        String fileName = inspector.resolveReportFileName(materialList);
        assertEquals(jobName.toString() + "-" +
                        jobTimestamp.toString() + ".html",
                fileName);
    }

    @Test
    public void test_resolveReportFileName_MaterialProductGroup() {
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right)
                .build();
        String fileName = inspector.resolveReportFileName(mpg);
        assertEquals(mpg.getJobName() + "-" +
                        mpg.getJobTimestampOfReduceResult().toString() +
                        ".html",
                fileName);
    }
}
