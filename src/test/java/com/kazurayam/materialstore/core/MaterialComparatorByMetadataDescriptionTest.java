package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialComparatorByMetadataDescriptionTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(MaterialComparatorByMetadataDescriptionTest.class);
    private static Store store;
    private static final JobName jobName = new JobName(MaterialComparatorByMetadataDescriptionTest.class.getSimpleName());
    private static JobTimestamp jobTimestamp;
    private static Map<String, Metadata> fixture = new HashMap<>();

    private Material m0;
    private Material m1;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        Path classOutputDir = too.cleanClassOutputDirectory();
        jobTimestamp = JobTimestamp.now();
        store = Stores.newInstance(classOutputDir);
        // create fixture
        fixture = createFixture();
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
    }

    @BeforeEach
    public void beforeEach() throws MaterialstoreException {
        m0 = store.selectSingle(jobName, jobTimestamp,
                new QueryOnMetadata.Builder(fixture.get("Google")).build());
        assert m0 != null;
        m1 = store.selectSingle(jobName, jobTimestamp,
                new QueryOnMetadata.Builder(fixture.get("DuckDuckGo")).build());
        assert m1 != null;
    }

    @Test
    public void test_noSortKeys() throws MaterialstoreException {
        MaterialComparatorByMetadataDescription comparator =
                new MaterialComparatorByMetadataDescription();
        assertTrue(comparator.compare(m0, m1) < 0);
    }

    @Test
    public void test_withSortKeys() {
        SortKeys sortKeys = new SortKeys("URL.host");
        MaterialComparatorByMetadataDescription comparator =
                new MaterialComparatorByMetadataDescription(sortKeys);
        assertTrue(comparator.compare(m0, m1) > 0);
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
}
