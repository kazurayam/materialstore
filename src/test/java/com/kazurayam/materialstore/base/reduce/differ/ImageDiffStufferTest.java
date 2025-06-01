package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
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
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageDiffStufferTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(ImageDiffStufferTest.class);

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path classOutputDir = too.cleanClassOutputDirectory();
    }

    private MaterialProductGroup prepareFixture(Store store, JobName jobName) throws MaterialstoreException {
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        SampleFixtureInjector.injectSampleResults(store, jobName);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map).build());
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map1).build());
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL", "URL.host").build();
        assertNotNull(mpg);
        Assertions.assertEquals(2, mpg.size(), JsonUtil.prettyPrint(mpg.toString()));
        return mpg;
    }

    @Test
    public void test_generateDiff() throws MaterialstoreException, IOException {
        String methodName = "test_stuffDiff";
        Path methodOutputDirectory =
                too.cleanMethodOutputDirectory(methodName);
        Store store = new StoreImpl(methodOutputDirectory.resolve("store"));
        JobName jobName = new JobName(methodName);
        //
        MaterialProductGroup mpg = prepareFixture(store, jobName);
        //
        MaterialProduct stuffed =
                new ImageDiffStuffer(store).generateDiff(mpg.get(0));
        assertNotNull(stuffed);
        assertNotNull(stuffed.getDiff());
        Assertions.assertTrue(stuffed.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }

    @Test
    public void test_DiffColorGray() throws MaterialstoreException, IOException {
        String methodName = "test_DiffColorGray";
        Path methodOutputDirectory =
                too.cleanMethodOutputDirectory(methodName);
        Store store = new StoreImpl(methodOutputDirectory.resolve("store"));
        JobName jobName = new JobName(methodName);
        //
        MaterialProductGroup mpg = prepareFixture(store, jobName);
        //
        MaterialProduct stuffed =
                new ImageDiffStuffer(store).generateDiff(mpg.get(0));
        assertNotNull(stuffed);
        assertNotNull(stuffed.getDiff());
        Assertions.assertTrue(stuffed.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }

}
