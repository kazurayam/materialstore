package com.kazurayam.materialstore.reduce


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class MProductGroup_issue87Test {

    private static Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixture/issue#80")

    private static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(MProductGroup_issue87Test.class.getName())

    private static Store store

    private JobName jobName
    private JobTimestamp timestampP
    private JobTimestamp timestampD

    MaterialList left
    MaterialList right
    MProductGroup mProductGroup

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
    void setup() {
        jobName = new JobName("MyAdmin_visual_inspection_twins")
        timestampP = new JobTimestamp("20220128_191320")
        timestampD = new JobTimestamp("20220128_191342")

        left = store.select(jobName, timestampP,
                QueryOnMetadata.builder(["profile": "MyAdmin_ProductionEnv"]).build()
        )
        assert left.size() == 8
        right = store.select(jobName, timestampD,
                QueryOnMetadata.builder(["profile": "MyAdmin_DevelopmentEnv"]).build()
        )
        assert right.size() == 8

        mProductGroup =
                MProductGroup.builder(left, right)
                        .ignoreKeys("profile", "URL.host")
                        .identifyWithRegex(["URL.query":"\\w{32}"])
                        .build()
    }

    @Test
    void test_getDescription_short() {
        String desc = mProductGroup.getDescription(false)
        println JsonOutput.prettyPrint(desc)
    }

    @Test
    void test_getDescription_full() {
        String desc = mProductGroup.getDescription(true)
        println JsonOutput.prettyPrint(desc)
    }



    @Test
    void test_getJobTimestampLeft() {
        assertEquals(new JobTimestamp("20220128_191320"),
                mProductGroup.getJobTimestampLeft())
    }

    @Test
    void test_getJobTimestampRight() {
        assertEquals(new JobTimestamp("20220128_191342"),
                mProductGroup.getJobTimestampRight())
    }

    @Test
    void test_getJobTimestampPrevious() {
        assertEquals(new JobTimestamp("20220128_191320"),
                mProductGroup.getJobTimestampPrevious())
    }

    @Test
    void test_getJobTimestampFollowing() {
        assertEquals(new JobTimestamp("20220128_191342"),
                mProductGroup.getJobTimestampFollowing())
    }

    @Test
    void test_getQueryOnMetadataList() {

        List<QueryOnMetadata> queryList = mProductGroup.getQueryOnMetadataList();
        assertEquals(8, queryList.size())
        //
        queryList.each { mp ->
            println mp.toString()
        }
        /* before issue#87, the output was
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2", "URL.port":"80", "URL.protocol":"https", "URL.query":"30af91bf14e37666a085fb8a161ff36d"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/ajax/libs/jquery/1.12.4/jquery.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/umineko-1960x1960.jpg", "URL.port":"80", "URL.protocol":"http"}

after issue87, the output is
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/ajax/libs/jquery/1.12.4/jquery.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2", "URL.port":"80", "URL.protocol":"https", "URL.query":"30af91bf14e37666a085fb8a161ff36d"}
{"URL.path":"/umineko-1960x1960.jpg", "URL.port":"80", "URL.protocol":"http"}
         */
        assertTrue(queryList.get(0).toString().startsWith("{\"URL.path\":\"/\""), queryList.get(0).toString())
        assertTrue(queryList.get(2).toString().startsWith("{\"URL.path\":\"/ajax"), queryList.get(2).toString())
    }
}
