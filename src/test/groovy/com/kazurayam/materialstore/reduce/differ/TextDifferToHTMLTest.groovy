package com.kazurayam.materialstore.reduce.differ

import com.github.difflib.DiffUtils
import com.github.difflib.patch.Patch
import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.TextDiffUtil
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.StoreImpl
import com.kazurayam.materialstore.reduce.MaterialProduct
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.report.AbstractReporterTest
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class TextDifferToHTMLTest extends AbstractReporterTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(TextDifferToHTMLTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private static Store store

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Path root = outputDir.resolve("store")
        store = new StoreImpl(root)
    }


    //@Disabled
    @Test
    void test_makeMProductMB() {
        JobName jobName = new JobName("test_makeMProductMB")
        TestFixtureUtil.setupFixture(store, jobName)
        MaterialProduct mProductMB = injectDiffAsMaterialProductMB(store, jobName)
        assertNotNull(mProductMB)
        assertNotNull(mProductMB.getDiff())
        assertTrue(mProductMB.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, mProductMB.getDiff())
    }

    @Test
    void test_makeMProduct_and_compare() {
        JobName jobName = new JobName("test_makeMProduct_and_compare")
        TestFixtureUtil.setupFixture(store, jobName)
        MaterialProduct mProductMB = injectDiffAsMaterialProductMB(store, jobName)
        MaterialProduct mProductFM = injectDiffAsMaterialProductFM(store, jobName)
        Path jobNameDir = store.getRoot().resolve(jobName.toString())
        store.retrieve(mProductMB.getDiff(), jobNameDir.resolve("byMB.html"))
        store.retrieve(mProductFM.getDiff(), jobNameDir.resolve("byFM.html"))
        // make diff, write into a file in Markdown format
        Path diff = jobNameDir.resolve("diff.md")
        List<String> htmlByMB = trimLines(store.readAllLines(mProductMB.getDiff()))
        List<String> htmlByFM = trimLines(store.readAllLines(mProductFM.getDiff()))
        TextDiffUtil.writeDiff(htmlByMB, htmlByFM, diff,
                Arrays.asList("")
        )
        // compute the patch
        Patch<String> patch = DiffUtils.diff(htmlByMB, htmlByFM);
        patch.getDeltas().forEach({it -> System.out.println(it)});
        assertEquals(4, patch.getDeltas().size());
    }


    /**
     * using MarkupBuilder
     * @param jobName
     * @return
     */
    private static MaterialProduct injectDiffAsMaterialProductMB(Store store, JobName jobName) {
        MProductGroup prepared = prepareMProductGroup(store, jobName);
        TextDifferToHTMLMB instance = new TextDifferToHTMLMB(store);
        instance.enablePrettyPrinting(true);
        return instance.injectDiff(prepared.get(0));
    }

    /**
     * using FreeMarker
     * @param jobName
     * @return
     */
    private static MaterialProduct injectDiffAsMaterialProductFM(Store store, JobName jobName) {
        MProductGroup prepared = prepareMProductGroup(store, jobName);
        TextDifferToHTML instance = new TextDifferToHTML(store);
        instance.enablePrettyPrinting(true);
        return instance.injectDiff(prepared.get(0));
    }

    /**
     *
     * @param jobName
     * @return
     */
    private static MProductGroup prepareMProductGroup(Store store, JobName jobName) {
        Objects.requireNonNull(jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        MaterialList expected = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder([
                        "category":"page source", "profile": "ProductionEnv"])
                        .build(),
                FileType.HTML)
        assertEquals(1, expected.size())

        MaterialList actual = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder([
                        "category":"page source", "profile": "DevelopmentEnv"])
                        .build(),
                FileType.HTML)
        assertEquals(1, actual.size())

        MProductGroup prepared =
                MProductGroup.builder(expected, actual)
                        .ignoreKeys("profile", "URL.host")
                        .build()
        assertNotNull(prepared)
        assertEquals(1, prepared.size())
        return prepared
    }
}