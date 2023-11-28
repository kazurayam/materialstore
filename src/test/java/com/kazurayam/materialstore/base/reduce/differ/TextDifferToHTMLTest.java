package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.base.report.AbstractReporterTest;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.StoreImpl;
import com.kazurayam.materialstore.zest.TestFixtureUtil;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Objects;

public class TextDifferToHTMLTest extends AbstractReporterTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(TextDifferToHTMLTest.class);
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_makeMProduct() throws MaterialstoreException {
        JobName jobName = new JobName("test_makeMProduct");
        TestFixtureUtil.setupFixture(store, jobName);
        MaterialProduct mProductFM = injectDiffAsMaterialProductFM(store, jobName);
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        store.retrieve(mProductFM.getDiff(), jobNameDir.resolve("byFM.html"));
    }

    /**
     * using FreeMarker
     *
     */
    private static MaterialProduct injectDiffAsMaterialProductFM(Store store, JobName jobName) throws MaterialstoreException {
        MaterialProductGroup reducedMPG = prepareMPG(store, jobName);
        TextDifferToHTML instance = new TextDifferToHTML(store);
        instance.enablePrettyPrinting(false);
        return instance.stuffDiff(reducedMPG.get(0));
    }

    /**
     */
    private static MaterialProductGroup prepareMPG(Store store, JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("category", "page source");
        map.put("environment", "ProductionEnv");
        MaterialList expected = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map).build());
        Assertions.assertEquals(1, expected.size());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("category", "page source");
        map1.put("environment", "DevelopmentEnv");
        MaterialList actual = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map1).build());
        Assertions.assertEquals(1, actual.size());

        MaterialProductGroup reducedMPG = MaterialProductGroup.builder(expected, actual).ignoreKeys("environment", "URL.host").build();
        Assertions.assertNotNull(reducedMPG);
        Assertions.assertEquals(1, reducedMPG.size());
        return reducedMPG;
    }
}
