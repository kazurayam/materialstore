package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.*
import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class TextDifferToHTMLTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(TextDifferToHTMLTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")


    @Test
    void test_makeDiff() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        //
        MaterialList expected = storeImpl.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap([
                        "category":"page source", "profile": "ProductionEnv"])
                        .build(),
                FileType.HTML)

        MaterialList actual = storeImpl.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap([
                        "category":"page source","profile": "DevelopmentEnv"])
                        .build(),
                FileType.HTML)

        DiffArtifacts diffArtifacts =
                storeImpl.zipMaterials(expected, actual,
                        MetadataIgnoredKeys.of("profile", "URL", "URL.host"))
        assertNotNull(diffArtifacts)
        assertEquals(1, diffArtifacts.size())
        //
        DiffArtifact stuffed = new TextDifferToHTML(root).makeDiffArtifact(diffArtifacts.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertTrue(stuffed.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }

    @Test
    void test_splitStringWithTags() {
        String OT = TextDifferToHTML.OLD_TAG
        String NT = TextDifferToHTML.NEW_TAG
        String given = "  if ${OT}foo${OT} is ${NT}bar${NT} {"
        List<String> expected = [
                "  if ",
                OT,
                "foo",
                OT,
                " is ",
                NT,
                "bar",
                NT,
                " {"
        ]
        assertEquals(expected, TextDifferToHTML.splitStringWithOldNewTags(given))
    }

    @Test
    void test_markupSegments() {
        String OT = TextDifferToHTML.OLD_TAG
        String NT = TextDifferToHTML.NEW_TAG
        List<String> segments = [
                "    <link ", OT, "href=\"https:", OT, "//katalon", OT, "-demo-cura", OT,
                ".", NT, "herokuapp.", NT, "com//css/theme.css\" rel=\"stylesheet\">"
        ]
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        // no do it
        TextDifferToHTML.markupSegments(mb, segments)
        String markup = sw.toString()
        assertTrue(markup.contains("<span class=\'deletion\'>"))
        assertTrue(markup.contains("<span class=\'insertion\'>"))
        assertTrue(markup.contains("<span class=\'unchanged\'>"))
        assertTrue(markup.contains("""<span class='unchanged'>    &lt;link </span>"""))
        assertTrue(markup.contains("""<span class='deletion'>href="https:</span>"""))
        assertTrue(markup.contains("""<span class='unchanged'>//katalon</span>"""))
        assertTrue(markup.contains("""<span class='deletion'>-demo-cura</span>"""))
        assertTrue(markup.contains("""<span class='unchanged'>.</span>"""))
        assertTrue(markup.contains("""<span class='insertion'>herokuapp.</span>"""))
        assertTrue(markup.contains("""<span class='unchanged'>com//css/theme.css" rel="stylesheet"&gt;</span>"""))
    }
}