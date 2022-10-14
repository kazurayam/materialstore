package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

public class MaterialProductGroup_issue87Test {

    private static final Path fixtureDir = Paths.get(".").resolve("src/test/fixture/issue#80");
    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(MaterialProductGroup_issue87Test.class.getName());
    private static Store store;
    private MaterialList left;
    private MaterialList right;
    private MaterialProductGroup mpg;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        FileUtils.copyDirectory(fixtureDir.toFile(), storePath.toFile());
        store = Stores.newInstance(storePath);
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");

        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        assert left.size() == 8;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        assert right.size() == 8;

        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("URL.query", "\\w{32}");
        mpg = MaterialProductGroup.builder(left, right).ignoreKeys("profile", "URL.host").identifyWithRegex(map2).build();
    }

    @Test
    public void test_toStringRepresentation_short() {
        String desc = mpg.toVariableJson(new SortKeys(), false);
        System.out.println("[test_getDescription_short]\n" + JsonUtil.prettyPrint(desc));
    }

    @Test
    public void test_toStringRepresentation_full() {
        String desc = mpg.toVariableJson(new SortKeys(), true);
        System.out.println("[test_getDescription_full]\n" + JsonUtil.prettyPrint(desc));
    }

    @Test
    public void test_getJobTimestampLeft() {
        Assertions.assertEquals(new JobTimestamp("20220128_191320"), mpg.getJobTimestampLeft());
    }

    @Test
    public void test_getJobTimestampRight() {
        Assertions.assertEquals(new JobTimestamp("20220128_191342"), mpg.getJobTimestampRight());
    }

    @Test
    public void test_getJobTimestampPrevious() {
        Assertions.assertEquals(new JobTimestamp("20220128_191320"), mpg.getJobTimestampPrevious());
    }

    @Test
    public void test_getJobTimestampFollowing() {
        Assertions.assertEquals(new JobTimestamp("20220128_191342"), mpg.getJobTimestampFollowing());
    }

    @Test
    public void test_getQueryOnMetadataList() {

        List<QueryOnMetadata> queryList = mpg.getQueryOnMetadataList();
        Assertions.assertEquals(8, queryList.size());
        //
        System.out.println("[test_getQueryOnMetadataList]");
        queryList.forEach(
                System.out::println
        );
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
        Assertions.assertTrue(queryList.get(0).toString().startsWith("{\"URL.path\":\"/\""), queryList.get(0).toString());
        Assertions.assertTrue(queryList.get(2).toString().startsWith("{\"URL.path\":\"/ajax"), queryList.get(2).toString());
    }

    public MaterialList getLeft() {
        return left;
    }

    public void setLeft(MaterialList left) {
        this.left = left;
    }

    public MaterialList getRight() {
        return right;
    }

    public void setRight(MaterialList right) {
        this.right = right;
    }

    public MaterialProductGroup getMpg() {
        return mpg;
    }

    public void setMpg(MaterialProductGroup mpg) {
        this.mpg = mpg;
    }
}
