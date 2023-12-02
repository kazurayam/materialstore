package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialListTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialListTest.class);
    private static Store store;
    private static final JobName jobName = new JobName(MaterialListTest.class.getSimpleName());
    private static JobTimestamp jobTimestamp;
    private Material material;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        Path outputDir = too.getClassOutputDirectory();
        too.cleanClassOutputDirectory();
        store = Stores.newInstance(outputDir);
        jobTimestamp = JobTimestamp.now();
        // create fixture
        Map<String, Metadata> fixture = createFixture();
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
    }

    private static Map<String, Metadata> createFixture() {
        Map<String, Metadata> fixture = new HashMap<>();
        //
        Map<String, String> m1 = new HashMap<>();
        m1.put("URL.host","www.google.com");
        m1.put("timestamp", "20221010-132801");
        m1.put("key1", "value1");
        m1.put("zzz", "zzz");
        fixture.put("Google", new Metadata.Builder(m1).build());
        //
        Map<String, String> m2 = new HashMap<>();
        m2.put("URL.host","duckduckgo.com");
        m2.put("timestamp", "20221010-132806");
        m2.put("key1", "value1");
        m2.put("zzz", "zzz");
        fixture.put("DuckDuckGo", new Metadata.Builder(m2).build());
        //
        return fixture;
    }

    @BeforeEach
    public void beforeEach() {
        String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"profile\":\"DevelopmentEnv\",\"URL\":\"http://demoaut-mimic.kazurayam.com/\"}";
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        material = new Material(store, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry);
    }

    @Test
    public void test_countMaterialsWithIdStartingWith() {
        MaterialList materialList = new MaterialList(jobName, jobTimestamp, QueryOnMetadata.ANY);
        Assertions.assertEquals(0, materialList.countMaterialsWithIdStartingWith("6141b40"));
        materialList.add(material);
        Assertions.assertEquals(1, materialList.countMaterialsWithIdStartingWith("6141b40"));
    }

    @Test
    public void test_smoke() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        materialList.add(material);
        Assertions.assertEquals(1, materialList.size());
        assertTrue(materialList.contains(material));
    }

    @Test
    public void test_getQueryOnMetadata() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        materialList.add(material);
        Assertions.assertNotNull(materialList.getQueryOnMetadata());
        //println materialList.getQueryOnMetadata().toString()
    }

    /**
     * reproducing the issue https://github.com/kazurayam/VisualInspectionOfExcelAndPDF/issues/11
     *
     * When a materialList is empty, the call "materialList.get(0)" threw an IndexOutOfBoundsException.
     * How to workaround it?
     * The call "materialList.get(x)" should not throw and exception, but silently return null.
     */
    @Test
    public void test_get_shouldNotThrowIndexOutOfBoundsException() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        Material m = materialList.get(0);
        assertNull(m);
    }

    @Test
    public void test_getJobName() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        Assertions.assertEquals(jobName, materialList.getJobName());
    }

    @Test
    public void test_getJobTimestamp() {
        JobTimestamp now = JobTimestamp.now();
        MaterialList materialList = new MaterialList(jobName, now, QueryOnMetadata.ANY);
        Assertions.assertEquals(now, materialList.getJobTimestamp());
    }

    @Test
    public void test_toString() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        materialList.add(material);
        String str = materialList.toString();
        Assertions.assertNotNull(str);
        //println str
    }

    @Test
    public void test_toJson() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), QueryOnMetadata.ANY);
        materialList.add(material);
        String json = materialList.toJson();
        Assertions.assertNotNull(json);
        System.out.println(json);
    }

    @Test
    public void test_toTemplateModel_noArg() throws MaterialstoreException {
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        Map<String, Object> model = materialList.toTemplateModel();
        System.out.println(model.toString());
        Assertions.assertNotNull(model);
    }

    @Test
    public void test_toTemplateModel_withSortKeys() throws MaterialstoreException {
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        SortKeys sortKeys = new SortKeys("timestamp","zzz");
        Map<String, Object> model = materialList.toTemplateModel(sortKeys);
        System.out.println(model.toString());
        Assertions.assertNotNull(model);
    }

    @Test
    public void test_toTemplateModelAsJson_noArg() throws IOException, MaterialstoreException {
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        String json = materialList.toTemplateModelAsJson(true);
        Files.write(too.getClassOutputDirectory().resolve("test_toTemplateModelAsJSON.json"),
                json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void test_toTemplateModelAsJson_withSortKeys() throws IOException, MaterialstoreException {
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        SortKeys sortKeys = new SortKeys("timestamp","zzz");
        String json = materialList.toTemplateModelAsJson(sortKeys, true);
        Files.write(too.getClassOutputDirectory().resolve("test_toTemplateModelAsJSON.json"),
                json.getBytes(StandardCharsets.UTF_8));
    }

}
