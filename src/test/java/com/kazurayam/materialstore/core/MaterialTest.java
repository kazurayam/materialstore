package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialTest {
    private final static TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialTest.class);
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"profile\":\"DevelopmentEnv\",\"URL\":\"http://demoaut-mimic.kazurayam.com/\"}";
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine);
        Material material = new Material(store, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry);
        assertNotNull(material);
        Assertions.assertEquals("6141b40cfe9e7340a483a3097c4f6ff5d20e04ea",
                material.getIndexEntry().getID().toString());
        Assertions.assertEquals("6141b40",
                material.getShortID());
        Assertions.assertEquals(FileType.PNG,
                material.getIndexEntry().getFileType());
        Assertions.assertEquals("{\"profile\":\"DevelopmentEnv\", \"URL\":\"http://demoaut-mimic.kazurayam.com/\"}",
                material.getIndexEntry().getMetadata().getMetadataIdentification().toString());
        //
        //System.out.println(material.toString());
        Assertions.assertEquals(material, material);

    }

    /*
    @Test
    public void test_getNoMaterialFoundImageURL() throws MaterialstoreException {
        String url = Material.getNoMaterialFoundImageURL();
        System.out.println(url);
        assertNotNull(url);
        assertTrue(url.startsWith("data:image/png;base64,"));
    }
     */

    @Test
    public void test_getRelativePath_getRelativeURL() throws MaterialstoreException {
        JobName jobName = new JobName("test_getRelativePath");
        // copy the fixture files to the output dir
        SampleFixtureInjector.injectSampleResults(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        assertNotNull(material);
        //
        Path leftPath = Paths.get("test_getRelativePath/20210713_093357/objects/12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490.png");
        Assertions.assertEquals(leftPath, material.getRelativePath());
        //
        String leftURL = "test_getRelativePath/20210713_093357/objects/12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490.png";
        Assertions.assertEquals(leftURL, material.getRelativeURL());
    }

    @Test
    public void test_toFile_and_toURL() throws MaterialstoreException {
        JobName jobName = new JobName("test_toFile_and_toURL");
        // copy the fixture files to the output dir
        SampleFixtureInjector.injectSampleResults(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        assertNotNull(material);
        //
        assertTrue(Files.exists(material.toPath()));
        //
        URL url = material.toURL();
        assertTrue(url.toExternalForm().startsWith("file:/"), url.toString());
    }

    @Test
    public void test_toTemplateModel() throws MaterialstoreException {
        JobName jobName = new JobName("test_toTemplateModel");
        //
        SampleFixtureInjector.injectSampleResults(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        //
        Map<String, Object> model = material.toTemplateModel();
        //System.out.println(JsonUtil.prettyPrint(material.toJson()));
        assertNotNull(model);
        Assertions.assertEquals("test_toTemplateModel", model.get("jobName"));
        Assertions.assertEquals("20210713_093357", model.get("jobTimestamp"));
        Assertions.assertEquals("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490", model.get("id"));
        Assertions.assertEquals("png", model.get("fileType"));
        //
        Map<String, Object> metadata = material.getMetadata().toTemplateModel();
        Assertions.assertEquals("demoaut.katalon.com", ((Map)metadata.get("URL.host")).get("value"));
        Assertions.assertEquals("/", ((Map)metadata.get("URL.path")).get("value"));
        Assertions.assertEquals("http", ((Map)metadata.get("URL.protocol")).get("value"));
        Assertions.assertEquals("screenshot", ((Map)metadata.get("category")).get("value"));
        Assertions.assertEquals("ProductionEnv", ((Map)metadata.get("environment")).get("value"));

    }

    @Test
    public void test_isSimilarTo() {
        String sampleLine = "6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{\"profile\":\"DevelopmentEnv\",\"URL\":\"http://demoaut-mimic.kazurayam.com/\"}";
        IndexEntry indexEntry1 = IndexEntry.parseLine(sampleLine);
        Material material1 = new Material(store, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry1);
        IndexEntry indexEntry2 = IndexEntry.parseLine(sampleLine.replace("6141b40", "1020304"));
        Material material2 = new Material(store, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry2);
        assertTrue(material1.isSimilarTo(material2));
    }

    @Test
    public void test_loadNoCounterpartText() throws MaterialstoreException {
        byte[] bytes = Material.loadNoCounterpartText();
        assertTrue(bytes.length > 0);
    }

    @Test
    public void test_loadNoCounterpartPng() throws MaterialstoreException {
        byte[] bytes = Material.loadNoCounterpartPng();
        assertTrue(bytes.length > 0);
    }

    @Test
    public void test_toPath() throws MaterialstoreException {
        JobName jobName = new JobName("test_toPath");
        //
        SampleFixtureInjector.injectSampleResults(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        Jobber jobber = new Jobber(store, jobName, jobTimestamp);
        Material material = jobber.selectMaterial(
                new ID("12a1a5ee4d0ee278ef4998c3f4ebd4951e6d2490"));
        Assertions.assertTrue(Files.exists(material.toPath()));
    }

}
