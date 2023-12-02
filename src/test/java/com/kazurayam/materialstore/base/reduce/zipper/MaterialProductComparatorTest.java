package com.kazurayam.materialstore.base.reduce.zipper;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup_toVariableJsonTest;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialProductComparatorTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialProductComparatorTest.class);
    private static Store store;
    private static JobName jobName;
    private static JobTimestamp jobTimestamp1;
    private static JobTimestamp jobTimestamp2;
    private MaterialProduct mp1;
    private MaterialProduct mp2;
    private MaterialProductComparator comparator;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        //
        jobName = new JobName("MaterialProductComparatorTest");
        jobTimestamp1 = JobTimestamp.now();
        jobTimestamp2 = JobTimestamp.laterThan(jobTimestamp1);
        // create fixture
        Map<String, Metadata> fixture = MaterialProductGroup_toVariableJsonTest.createFixtureLeft();
        store.write(jobName, jobTimestamp1, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp1, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
        fixture = MaterialProductGroup_toVariableJsonTest.createFixtureRight();
        store.write(jobName, jobTimestamp2, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp2, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        MaterialList left;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "ProductionEnv");
        left = store.select(jobName, jobTimestamp1, QueryOnMetadata.builder(map1).build());
        //
        MaterialList right;
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("environment", "DevelopmentEnv");
        right = store.select(jobName, jobTimestamp2, QueryOnMetadata.builder(map2).build());
        //
        Map<String, String> q1 = new HashMap<>();
        q1.put("step","1");
        mp1 = new MaterialProduct.Builder(left.get(0), right.get(0), jobName, JobTimestamp.now())
                .setQueryOnMetadata(new QueryOnMetadata.Builder(q1).build())
                .build();
        //
        Map<String, String> q2 = new HashMap<>();
        q2.put("step","2");
        mp2 = new MaterialProduct.Builder(left.get(0), right.get(0), jobName, JobTimestamp.now())
                .setQueryOnMetadata(new QueryOnMetadata.Builder(q2).build())
                .build();
        //
        comparator = new MaterialProductComparator(new SortKeys("step", "timestamp", "URL.host"));
    }

    @Test
    public void test_mp1_is_smaller_than_mp2() {
        assertTrue(comparator.compare(mp1, mp2) < 0);
    }

    @Test
    public void test_mp1_equals_mp1() {
        assertEquals(0, comparator.compare(mp1, mp1));
    }

    @Test
    public void test_mp2_is_greater_than_mp1() {
        assertTrue(comparator.compare(mp2, mp1) > 0);
    }

}
