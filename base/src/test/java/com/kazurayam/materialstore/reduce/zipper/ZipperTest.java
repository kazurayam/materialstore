package com.kazurayam.materialstore.reduce.zipper;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipperTest {

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#80");

    private static final Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(ZipperTest.class.getName());

    private static Store store;
    private static JobName jobName;
    private static MaterialList left;
    private static MaterialList right;
    private MaterialProductGroup mpg;    // constructed with MaterialList.NULL_OBJECTs

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        Files.createDirectories(outputDir);
        FileUtils.copyDirectory(issue80Dir.toFile(), store.getRoot().toFile());
        //
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() > 0;
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() > 0;
    }

    @BeforeEach
    public void beforeEach() {
        mpg = MaterialProductGroup.builder(MaterialList.NULL_OBJECT, MaterialList.NULL_OBJECT).build();
    }

    @Test
    public void test_zipMaterials() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("URL.query", "\\w{32}");

        Zipper zipper =
                new Zipper(
                        new IgnoreMetadataKeys.Builder().ignoreKeys("profile", "URL.host", "URL.port", "URL.protocol").build(),
                        new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build());
        List<MaterialProduct> mProductList =
                zipper.zipMaterials(left, right, JobTimestamp.now());

        Assertions.assertNotNull(mProductList);
        for (MaterialProduct mProduct : mProductList) {
            //println JsonOutput.prettyPrint(mProduct.toString())
            Assertions.assertFalse(mProduct.getReducedTimestamp().equals(JobTimestamp.NULL_OBJECT));
        }
        assertEquals(8, mProductList.size());
    }

}
