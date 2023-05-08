package com.kazurayam.materialstore.base.reduce.zipper;

import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.FileTypeDiffability;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.core.metadata.QueryIdentification;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaterialProductTest {

    private static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(MaterialProductTest.class.getName());

    private static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixtures/issue#73/");

    private static Store store;
    private JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
    private JobTimestamp leftJobTimestamp = new JobTimestamp("20220125_140449");
    private JobTimestamp rightJobTimestamp = new JobTimestamp("20220125_140509");

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        //
        FileUtils.copyDirectory(fixtureDir.toFile(), root.toFile());
        //

    }

    @Test
    public void test_getQueryIdentification_more() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(3);
        map.put("URL.path", "/");
        map.put("environment", "Flaskr_ProductionEnv");
        map.put("step", "6");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        //
        SortKeys sortKeys = new SortKeys("step", "environment");
        MaterialProduct mProduct =
                new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        jobName, JobTimestamp.now()).setQueryOnMetadata(mp).build();

        QueryIdentification desc = mProduct.getQueryIdentification(sortKeys);
        assertEquals("{\"step\":\"6\", \"environment\":\"Flaskr_ProductionEnv\", \"URL.path\":\"/\"}", desc.toString());
    }

    @Test
    public void test_getQueryIdentification() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct =
                new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        jobName, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        assertEquals(
                "{\"URL.file\":\"/\", \"URL.host\":\"demoaut-mimic.kazurayam.com\"}",
                mProduct.getQueryIdentification().toString());
    }

    @Test
    public void test_getFileTypeDiffability() {
        MaterialProduct mProduct =
                new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        jobName, JobTimestamp.now())
                        .setQueryOnMetadata(QueryOnMetadata.ANY)
                        .build();
        assertEquals(FileTypeDiffability.UNABLE, mProduct.getFileTypeDiffability());
    }

    @Test
    public void test_toString() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct =
                new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        jobName, JobTimestamp.now())
                        .setQueryOnMetadata(mp).build();
        System.out.println(JsonUtil.prettyPrint(mProduct.toString()));
    }

    @Test
    public void test_toJson() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct =
                new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                        jobName, JobTimestamp.now())
                        .setQueryOnMetadata(mp).build();
        String json = mProduct.toJson(true);
        System.out.println(json);
    }

    @Test
    public void test_containsMaterialAt() throws MaterialstoreException {
        Material left = store.selectSingle(jobName, leftJobTimestamp, FileType.PNG);
        Material right = store.selectSingle(jobName, rightJobTimestamp, FileType.PNG);
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        MaterialProduct mp =
                new MaterialProduct.Builder(left, right, jobName, outJobTimestamp).build();
        assertEquals(-1, mp.containsMaterialAt(left));
        assertEquals(1, mp.containsMaterialAt(right));
        //
        Material html = store.selectSingle(jobName, leftJobTimestamp, FileType.HTML);
        assertEquals(0, mp.containsMaterialAt(html));
    }
}
