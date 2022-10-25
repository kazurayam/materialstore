package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
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

public class IdentityMapperTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(IdentityMapperTest.class.getName());
    private static final Path resultsDir = Paths.get(".").resolve("src/test/fixtures/sample_results");
    private Store store;
    private JobName jobName;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        jobName = new JobName("IdentityMapperTest");
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        Path target = root.resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), target.toFile());
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20210713_093357");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.host", "www.google.com");
        QueryOnMetadata query = QueryOnMetadata.builder(map).build();
        MaterialList mList = store.select(jobName, fixtureTimestamp, query);
        Assertions.assertEquals(1, mList.size());
        Material source = mList.get(0);
        //
        Mapper mapper = new IdentityMapper();
        mapper.setStore(store);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        MappedResultSerializer serializer = new MappedResultSerializer(store, jobName, jobTimestamp);
        mapper.setMappingListener(serializer);
        mapper.map(source);
        MaterialList serialized = store.select(jobName, jobTimestamp, QueryOnMetadata.builder().build());
        Assertions.assertTrue(serialized.size() > 0);
        Material material = serialized.get(0);
        Assertions.assertEquals("www.google.com", material.getMetadata().get("URL.host"));
    }


}
