package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.StoreImpl;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.util.JsonUtil;
import com.kazurayam.materialstore.util.TestFixtureUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class ImageDifferToPNGTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(ImageDifferToPNGTest.class.getName());
    private static Store store;

    @BeforeAll
    public static void beforeAll() {
        Path root = outputDir.resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_injectDiff() throws MaterialstoreException {
        JobName jobName = new JobName("test_makeDiff");
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        TestFixtureUtil.setupFixture(store, jobName);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map).build());
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map1).build());
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("profile", "URL", "URL.host").build();
        Assertions.assertNotNull(mpg);
        Assertions.assertEquals(2, mpg.size(), JsonUtil.prettyPrint(mpg.toString()));
        //
        MaterialProduct stuffed = new ImageDifferToPNG(store).stuffDiff(mpg.get(0));
        Assertions.assertNotNull(stuffed);
        Assertions.assertNotNull(stuffed.getDiff());
        Assertions.assertTrue(stuffed.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }


}
