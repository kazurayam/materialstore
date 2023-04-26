package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.StoreImpl;
import com.kazurayam.materialstore.util.JsonUtil;
import com.kazurayam.materialstore.util.TestFixtureUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        map.put("environment", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map).build());
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map1).build());
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL", "URL.host").build();
        assertNotNull(mpg);
        Assertions.assertEquals(2, mpg.size(), JsonUtil.prettyPrint(mpg.toString()));
        //
        MaterialProduct stuffed = new ImageDifferToPNG(store).stuffDiff(mpg.get(0));
        assertNotNull(stuffed);
        assertNotNull(stuffed.getDiff());
        Assertions.assertTrue(stuffed.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }

}
