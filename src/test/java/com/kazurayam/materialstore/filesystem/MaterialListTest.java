package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.util.DotUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MaterialListTest {

    private static Path outputDir;
    private final JobName jobName = new JobName("MaterialListTest");
    private final QueryOnMetadata query = QueryOnMetadata.ANY;
    private Material material;

    @BeforeAll
    public static void beforeAll() throws IOException {
        outputDir = Paths.get("build/tmp/testOutput/").resolve(MaterialListTest.class.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    public void beforeEach() {
        String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"profile\":\"DevelopmentEnv\",\"URL\":\"http://demoaut-mimic.kazurayam.com/\"}";
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry);
    }

    @Test
    public void test_countMaterialsWithIdStartingWith() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        Assertions.assertEquals(0, materialList.countMaterialsWithIdStartingWith("6141b40"));
        materialList.add(material);
        Assertions.assertEquals(1, materialList.countMaterialsWithIdStartingWith("6141b40"));
    }

    @Test
    public void test_toTemplateModel() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        Map<String, Object> model = materialList.toTemplateModel();
        Assertions.assertNotNull(model);
    }

    @Test
    public void test_toTemplateModelAsJSON() throws IOException {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        String json = materialList.toTemplateModelAsJson();
        Files.write(outputDir.resolve("test_toTemplateModelAsJSON.json"),
                json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void test_smoke() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        materialList.add(material);
        Assertions.assertEquals(1, materialList.size());
        Assertions.assertTrue(materialList.contains(material));
    }

    @Test
    public void test_getQueryOnMetadata() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        materialList.add(material);
        Assertions.assertNotNull(materialList.getQueryOnMetadata());
        //println materialList.getQueryOnMetadata().toString()
    }

    @Test
    public void test_getJobName() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        Assertions.assertEquals(jobName, materialList.getJobName());
    }

    @Test
    public void test_getJobTimestamp() {
        JobTimestamp now = JobTimestamp.now();
        MaterialList materialList = new MaterialList(jobName, now, query);
        Assertions.assertEquals(now, materialList.getJobTimestamp());
    }

    @Test
    public void test_toString() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        materialList.add(material);
        String str = materialList.toString();
        Assertions.assertNotNull(str);
        //println str
    }

    @Test
    public void test_toJson() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query);
        materialList.add(material);
        String json = materialList.toJson();
        Assertions.assertNotNull(json);
        //System.out.println(json);
    }

    @Test
    public void test_toDot() throws IOException, MaterialstoreException, InterruptedException {
        Path fixtureDir = Paths.get("src/test/fixture/issue#73");
        Path root = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(),
                root.toFile());
        Store store = Stores.newInstance(root);
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp leftJobTimestamp = new JobTimestamp("20220125_140449");
        JobTimestamp rightJobTimestamp = new JobTimestamp("20220125_140509");
        MaterialList leftML = store.select(
                jobName, leftJobTimestamp, QueryOnMetadata.ANY);
        MaterialList rightML = store.select(
                jobName, rightJobTimestamp, QueryOnMetadata.ANY);

        // generate *.dot files of MaterialList objects
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        Metadata leftMetadata = Metadata.builder().put("side", "left").build();
        String leftDot = leftML.toDot();
        store.write(jobName, outJobTimestamp, FileType.DOT, leftMetadata, leftDot);
        Metadata rightMetadata = Metadata.builder().put("side", "right").build();
        String rightDot = rightML.toDot();
        store.write(jobName, outJobTimestamp, FileType.DOT, rightMetadata, rightDot);

        // generate PNG images of MaterialList objects
        Material leftPng = DotUtil.storeGraph(store, jobName, outJobTimestamp, leftMetadata, leftDot);
        Material rightPng = DotUtil.storeGraph(store, jobName, outJobTimestamp, rightMetadata, rightDot);
    }


}
