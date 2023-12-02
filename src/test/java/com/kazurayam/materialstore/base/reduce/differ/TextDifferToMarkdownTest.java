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
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class TextDifferToMarkdownTest {

    private static final TestOutputOrganizer too = TestOutputOrganizerFactory.create(TextDifferToMarkdownTest.class);

    @Test
    public void test_injectDiff() throws MaterialstoreException, IOException {
        too.cleanClassOutputDirectory();
        Path root = too.getClassOutputDirectory().resolve("store");
        Store store = new StoreImpl(root);
        JobName jobName = new JobName("test_makeDiff");
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        SampleFixtureInjector.injectSampleResults(store, jobName);
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("category", "page source");
        map.put("environment", "ProductionEnv");
        MaterialList left = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map).build());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("category", "page source");
        map1.put("environment", "DevelopmentEnv");
        MaterialList right = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map1).build());

        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL", "URL.host").build();
        Assertions.assertNotNull(mpg);
        Assertions.assertEquals(1, mpg.size());
        //
        MaterialProduct stuffed = new TextDifferToMarkdown(store).stuffDiff(mpg.get(0));
        Assertions.assertNotNull(stuffed);
        Assertions.assertNotNull(stuffed.getDiff());
        Assertions.assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff());
    }

}
