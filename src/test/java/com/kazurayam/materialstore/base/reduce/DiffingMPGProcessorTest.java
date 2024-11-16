package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.differ.ImageDiffStuffer;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.StoreImpl;
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class DiffingMPGProcessorTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(DiffingMPGProcessorTest.class);
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path root = too.cleanClassOutputDirectory().resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_Builder_differFor() {
        DiffingMPGProcessor differDriver =
                new DiffingMPGProcessor.Builder(store)
                        .differFor(FileType.JPEG, new ImageDiffStuffer(store))
                        .build();
        Assertions.assertTrue(differDriver.hasDiffer(FileType.JPEG));
    }

    @Test
    public void test_TextDiffer() throws MaterialstoreException {
        JobName jobName = new JobName("test_TextDiffer");
        SampleFixtureInjector.injectSampleResults(store, jobName);

        JobTimestamp timestamp1 = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "ProductionEnv");
        MaterialList left = store.select(jobName, timestamp1,  FileType.HTML, QueryOnMetadata.builder(map).build());
        Assertions.assertEquals(1, left.size());

        JobTimestamp timestamp2 = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "DevelopmentEnv");
        MaterialList right = store.select(jobName, timestamp2, FileType.HTML, QueryOnMetadata.builder(map1).build());
        Assertions.assertEquals(1, right.size());

        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL", "URL.host").build();
        Assertions.assertNotNull(mpg);
        Assertions.assertEquals(1, mpg.size());
        //
        DiffingMPGProcessor differDriver = new DiffingMPGProcessor.Builder(store).build();
        MaterialProductGroup resolved = differDriver.process(mpg);
        Assertions.assertEquals(1, resolved.size());
    }

    @Test
    public void test_ImageDiffer() throws MaterialstoreException {
        JobName jobName = new JobName("test_ImageDiffer");
        SampleFixtureInjector.injectSampleResults(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map).build());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map1).build());

        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL", "URL.host").build();
        Assertions.assertNotNull(mpg);
        Assertions.assertEquals(2, mpg.size());
        //
        DiffingMPGProcessor differDriver = new DiffingMPGProcessor.Builder(store).build();
        MaterialProductGroup resolved = differDriver.process(mpg);
        Assertions.assertNotNull(resolved);
        Assertions.assertEquals(2, resolved.size());
    }
}
