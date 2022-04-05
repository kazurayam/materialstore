package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TestFixtureUtil;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.StoreImpl;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import com.kazurayam.materialstore.util.JsonUtil;
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
        MaterialList left = store.select(jobName, jobTimestamp, QueryOnMetadata.builder(map).build(), FileType.PNG);
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, QueryOnMetadata.builder(map1).build(), FileType.PNG);
        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL", "URL.host").build();
        Assertions.assertNotNull(mProductGroup);
        Assertions.assertEquals(2, mProductGroup.size(), JsonUtil.prettyPrint(mProductGroup.toString()));
        //
        MaterialProduct stuffed = new ImageDifferToPNG(store).injectDiff(mProductGroup.get(0));
        Assertions.assertNotNull(stuffed);
        Assertions.assertNotNull(stuffed.getDiff());
        Assertions.assertTrue(stuffed.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }


}
