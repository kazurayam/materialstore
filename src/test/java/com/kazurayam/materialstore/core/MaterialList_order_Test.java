package com.kazurayam.materialstore.core;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test the sort(SortKeys) method of the Materialist class
 */
public class MaterialList_order_Test {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialList_order_Test.class);
    private static Store store;
    private static final JobName jobName =
            new JobName(MaterialList_order_Test.class.getSimpleName());
    private static JobTimestamp jobTimestamp;

    private MaterialList ml;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        too.cleanOutputSubDirectory();
        jobTimestamp = JobTimestamp.now();
        store = Stores.newInstance(too.resolveOutput("store"));
        // create fixture
        Map<String, Metadata> fixture = createFixture();
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
    }

    private static Map<String, Metadata> createFixture() {
        Map<String, Metadata> fixture = new HashMap<>();
        //
        Map<String, String> m1 = new HashMap<>();
        m1.put("URL.host","www.google.com");
        m1.put("timestamp", "20221010-132801");
        fixture.put("Google", new Metadata.Builder(m1).build());
        //
        Map<String, String> m2 = new HashMap<>();
        m2.put("URL.host","duckduckgo.com");
        m2.put("timestamp", "20221010-132806");
        fixture.put("DuckDuckGo", new Metadata.Builder(m2).build());
        //
        return fixture;
    }

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        ml = store.select(jobName, jobTimestamp, FileType.TXT);
    }

    /**
     * The timestamp of www.google.com is earlier than duckduckgo.com
     */
    @Test
    public void test_noSortKeys() throws MaterialstoreException {
        SortKeys emptySortKeys = new SortKeys();
        ml.order(emptySortKeys);
        assertEquals("www.google.com", ml.get(0).getMetadata().get("URL.host"));
        assertEquals("duckduckgo.com", ml.get(1).getMetadata().get("URL.host"));
    }

    /**
     * please note that DuckDucGo comes before Google when SortKeys("URL.host") is given.
     */
    @Test
    public void test_withSortKeys() {
        SortKeys someSortKeys = new SortKeys("URL.host");
        ml.order(someSortKeys);
        assertEquals("duckduckgo.com", ml.get(0).getMetadata().get("URL.host"));
        assertEquals("www.google.com", ml.get(1).getMetadata().get("URL.host"));
    }
}
