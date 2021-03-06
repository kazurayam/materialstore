package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.StoreImpl;
import com.kazurayam.materialstore.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.util.TestFixtureUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class DiffingMPGProcessorTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(DiffingMPGProcessorTest.class.getName());
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        Files.createDirectories(root);
        store = new StoreImpl(root);
    }

    @Test
    public void test_Builder_differFor() {
        DiffingMPGProcessor differDriver =
                new DiffingMPGProcessor.Builder(store)
                        .differFor(FileType.JPEG, new ImageDifferToPNG(store))
                        .build();
        Assertions.assertTrue(differDriver.hasDiffer(FileType.JPEG));
    }

    @Test
    public void test_TextDiffer() throws MaterialstoreException {
        JobName jobName = new JobName("test_TextDiffer");
        TestFixtureUtil.setupFixture(store, jobName);

        JobTimestamp timestamp1 = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        MaterialList left = store.select(jobName, timestamp1,  FileType.HTML, QueryOnMetadata.builder(map).build());
        Assertions.assertEquals(1, left.size());

        JobTimestamp timestamp2 = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "DevelopmentEnv");
        MaterialList right = store.select(jobName, timestamp2, FileType.HTML, QueryOnMetadata.builder(map1).build());
        Assertions.assertEquals(1, right.size());

        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL", "URL.host").build();
        Assertions.assertNotNull(mProductGroup);
        Assertions.assertEquals(1, mProductGroup.size());
        //
        DiffingMPGProcessor differDriver = new DiffingMPGProcessor.Builder(store).build();
        MProductGroup resolved = differDriver.process(mProductGroup);
        Assertions.assertEquals(1, resolved.size());
    }

    @Test
    public void test_ImageDiffer() throws MaterialstoreException {
        JobName jobName = new JobName("test_ImageDiffer");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map).build());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.builder(map1).build());

        MProductGroup mProductGroup = MProductGroup.builder(left, right).ignoreKeys("profile", "URL", "URL.host").build();
        Assertions.assertNotNull(mProductGroup);
        Assertions.assertEquals(2, mProductGroup.size());
        //
        DiffingMPGProcessor differDriver = new DiffingMPGProcessor.Builder(store).build();
        MProductGroup resolved = differDriver.process(mProductGroup);
        Assertions.assertNotNull(resolved);
        Assertions.assertEquals(2, resolved.size());
    }


}
