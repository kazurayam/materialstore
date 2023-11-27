package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class IdentityMapperTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(IdentityMapperTest.class);
    private static final Path resultsDir =
            too.getProjectDir().resolve("src/test/fixtures/sample_results");
    private static Store store;
    private static JobName jobName;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        jobName = new JobName("IdentityMapperTest");
        Path target = root.resolve(jobName.toString());
        too.copyDir(resultsDir, target);
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
