package com.kazurayam.materialstore.reduce.differ;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TestFixtureUtil;
import com.kazurayam.materialstore.TextDiffUtil;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.StoreImpl;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import com.kazurayam.materialstore.report.AbstractReporterTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class TextDifferToHTMLTest extends AbstractReporterTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(TextDifferToHTMLTest.class.getName());

    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Path root = outputDir.resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_makeMProductMB() throws MaterialstoreException {
        JobName jobName = new JobName("test_makeMProductMB");
        TestFixtureUtil.setupFixture(store, jobName);
        MaterialProduct mProductMB = injectDiffAsMaterialProductMB(store, jobName);
        Assertions.assertNotNull(mProductMB);
        Assertions.assertNotNull(mProductMB.getDiff());
        Assertions.assertTrue(mProductMB.getDiffRatio() > 0);
        Assertions.assertNotEquals(Material.NULL_OBJECT, mProductMB.getDiff());
    }

    @Test
    public void test_makeMProduct_and_compare() throws MaterialstoreException, FileNotFoundException {
        JobName jobName = new JobName("test_makeMProduct_and_compare");
        TestFixtureUtil.setupFixture(store, jobName);
        MaterialProduct mProductMB = injectDiffAsMaterialProductMB(store, jobName);
        MaterialProduct mProductFM = injectDiffAsMaterialProductFM(store, jobName);
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        store.retrieve(mProductMB.getDiff(), jobNameDir.resolve("byMB.html"));
        store.retrieve(mProductFM.getDiff(), jobNameDir.resolve("byFM.html"));
        // make diff, write into a file in Markdown format
        Path diff = jobNameDir.resolve("diff.md");
        List<String> htmlByMB = trimLines(store.readAllLines(mProductMB.getDiff()));
        List<String> htmlByFM = trimLines(store.readAllLines(mProductFM.getDiff()));
        TextDiffUtil.writeDiff(htmlByMB, htmlByFM, diff, Collections.singletonList(""));
        // compute the patch
        Patch<String> patch = DiffUtils.diff(htmlByMB, htmlByFM);
        patch.getDeltas().forEach(System.out::println);
        Assertions.assertEquals(4, patch.getDeltas().size());
    }

    /**
     * using MarkupBuilder
     *
     */
    private static MaterialProduct injectDiffAsMaterialProductMB(Store store, JobName jobName) throws MaterialstoreException {
        MProductGroup prepared = prepareMProductGroup(store, jobName);
        TextDifferToHTMLMB instance = new TextDifferToHTMLMB(store);
        instance.enablePrettyPrinting(true);
        return instance.injectDiff(prepared.get(0));
    }

    /**
     * using FreeMarker
     *
     */
    private static MaterialProduct injectDiffAsMaterialProductFM(Store store, JobName jobName) throws MaterialstoreException {
        MProductGroup prepared = prepareMProductGroup(store, jobName);
        TextDifferToHTML instance = new TextDifferToHTML(store);
        instance.enablePrettyPrinting(true);
        return instance.injectDiff(prepared.get(0));
    }

    /**
     */
    private static MProductGroup prepareMProductGroup(Store store, JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("category", "page source");
        map.put("profile", "ProductionEnv");
        MaterialList expected = store.select(jobName, jobTimestamp, QueryOnMetadata.builder(map).build(), FileType.HTML);
        Assertions.assertEquals(1, expected.size());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("category", "page source");
        map1.put("profile", "DevelopmentEnv");
        MaterialList actual = store.select(jobName, jobTimestamp, QueryOnMetadata.builder(map1).build(), FileType.HTML);
        Assertions.assertEquals(1, actual.size());

        MProductGroup prepared = MProductGroup.builder(expected, actual).ignoreKeys("profile", "URL.host").build();
        Assertions.assertNotNull(prepared);
        Assertions.assertEquals(1, prepared.size());
        return prepared;
    }


}
