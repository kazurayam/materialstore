package issues.issues

import com.kazurayam.materialstore.DiffArtifacts
import com.kazurayam.materialstore.IgnoringMetadataKeys
import com.kazurayam.materialstore.JobName
import com.kazurayam.materialstore.JobTimestamp
import com.kazurayam.materialstore.MaterialList
import com.kazurayam.materialstore.MetadataPattern
import com.kazurayam.materialstore.Store
import com.kazurayam.materialstore.Stores
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.jupiter.api.Assertions.*

/**
 * Reproducing the issue #73 at https://github.com/kazurayam/materialstore/issues/73
 * and fixing it.
 */
class Issue73Test {

    static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/resources/fixture/issue#73")
    static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve("Issue73Test")
    static Store store
    static final JobName jobName = new JobName("MyAdmin_visual_inspection_twins")
    static final JobTimestamp timestampP = new JobTimestamp("20220125_140449")
    static final JobTimestamp timestampD = new JobTimestamp("20220125_140509")

    static final String leftUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"
    static final String rightUrl = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3-rc1/dist/js/bootstrap.bundle.min.js"

    MaterialList left
    MaterialList right

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            outputDir.toFile().deleteDir()
        }
        Files.createDirectories(outputDir)
        Path storePath = outputDir.resolve("store")
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile())
        store = Stores.newInstance(storePath)
    }

    @BeforeEach
    void beforeEach() {
        left = store.select(jobName, timestampP,
                MetadataPattern.builderWithMap(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                MetadataPattern.builderWithMap(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8
    }

    @Test
    void test_smoke() {
        Double criteria = 0.0d
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right, IgnoringMetadataKeys.of("profile", "URL.host"))
        int warnings = stuffedDiffArtifacts.countWarnings(criteria)
        // compile the report
        Path reportFile =
                store.reportDiffs(jobName, stuffedDiffArtifacts, criteria, jobName.toString() + "-index.html")
        assert stuffedDiffArtifacts.size() == 8
    }

    static final String regexOfHeader  = '(\\S+)'
    static final String regexOfVersion = '(\\d+\\.\\d+\\.\\d+(\\-[a-zA-Z][0-9a-zA-Z]*)?)'
    static final String regexOfTrailer = '(\\S*)'
    static final Pattern pathWithVersionParser = Pattern.compile(regexOfHeader + regexOfVersion + regexOfTrailer)

    @Test
    void test_finding_pathWithVersionParser() {
        URL url = new URL(rightUrl)
        String path = url.getPath()
        Matcher m = pathWithVersionParser.matcher(path)
        assert m.matches()
        assert m.groupCount() == 4
        assert m.group(1) == "/npm/bootstrap@"
        assert m.group(2) == "5.1.3-rc1"
        assert m.group(3) == "-rc1"
        assert m.group(4) == "/dist/js/bootstrap.bundle.min.js"
    }

    @Test
    void test_semantic_versions_valid() {
        List<String> validVersions = [ '5.1.3', '5.1.5-rc', '50.11.38' ]
        Pattern p = Pattern.compile('^' + regexOfVersion + '$')
        validVersions.each { it ->
            Matcher m = p.matcher(it)
            assert m.matches() : "input = ${it}"
        }
    }

    @Test
    void test_semantic_versions_invalid() {
        List<String> invalidVersions = ['1.0', '1-2-3', '4_5_6', '1.0.0.2', '2.5X']
        Pattern p = Pattern.compile('^' + regexOfVersion + '$')
        invalidVersions.each { it ->
            Matcher m = p.matcher(it)
            assert ! m.matches(): "input = ${it}"
        }
    }

    static final String translatePathComponentToRegex(String path) {
        return path
                .replace('/', "\\/")
                .replace('.', "\\.")
                .replace('\'', "\\\\'")
                .replace('(', "\\(")
                .replace(')', "\\)")
    }

    @Test
    void test_translatePathComponentToRegex() {
        assert translatePathComponentToRegex(
                '/npm/bootstrap@') == '\\/npm\\/bootstrap@'
        assert translatePathComponentToRegex(
                '/dist/js/bootstrap.bundle.min.js') == '\\/dist\\/js\\/bootstrap\\.bundle\\.min\\.js'
    }

    static String translatePathToRegex(String path) {
        Matcher m = pathWithVersionParser.matcher(path)
        if (m.matches()) {
            // the path has a semantic version
            String h = m.group(1)
            String t = m.group(4)
            StringBuilder sb = new StringBuilder()
            sb.append(translatePathComponentToRegex(h))
            sb.append(regexOfVersion)
            sb.append(translatePathComponentToRegex(t))
            return sb.toString()
        } else {
            // the path has no version
            return translatePathComponentToRegex(path)
        }
    }

    List pathFixtures = [
            "/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css",
            "/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2",
            "/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css",
            "/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js",
            "/ajax/libs/jquery/1.12.4/jquery.min.js",
            "/",
            "/umineko-1960x1960.jpg"
    ]

    @Test
    void test_translatePathToRegex() {
        pathFixtures.each { path ->
            Pattern p = Pattern.compile(translatePathToRegex(path))
            Matcher m = p.matcher(path)
            assert m.matches()
        }
    }

    @Test
    void test_full(){
        String left  = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"
        String right = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        URL leftUrl = new URL(left)
        String regex = translatePathToRegex(leftUrl.getPath())
        Pattern p = Pattern.compile(regex)
        URL rightUrl = new URL(right)
        Matcher m = p.matcher(rightUrl.getPath())
        assert m.matches()
    }

}
