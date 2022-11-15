package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.core.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This test concentrates on MProductGroup#toVariableJson() method.
 */
public class MaterialProductGroup_toVariableJsonTest {

    private static Path outputDir;
    private static Store store;
    private static JobName jobName;
    private static JobTimestamp jobTimestamp1;
    private static JobTimestamp jobTimestamp2;

    private static Map<String, Metadata> fixture = new HashMap<>();

    private MaterialProductGroup mpg;

    @BeforeAll
    public static void beforeAll() throws IOException, MaterialstoreException {
        outputDir = Paths.get(".").resolve("build/tmp/testOutput")
                .resolve(MaterialProductGroup_toVariableJsonTest.class.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path storePath = outputDir.resolve("store");
        store = Stores.newInstance(storePath);
        jobName = new JobName("toVariableJson_test");
        jobTimestamp1 = JobTimestamp.now();
        jobTimestamp2 = JobTimestamp.laterThan(jobTimestamp1);
        // create fixture
        fixture = createFixtureLeft();
        store.write(jobName, jobTimestamp1, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp1, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
        fixture = createFixtureRight();
        store.write(jobName, jobTimestamp2, FileType.TXT, fixture.get("Google"), "Google");
        store.write(jobName, jobTimestamp2, FileType.TXT, fixture.get("DuckDuckGo"), "DuckDuckGo");
    }

    /**
     * this method is used by MaterialProductComparatorTest as well
     * @return
     * @throws MalformedURLException
     */
    public static Map<String, Metadata> createFixtureLeft() throws MalformedURLException {
        Map<String, Metadata> fixture = new HashMap<>();
        //
        URL url1 = new URL("https://www.google.com/");
        Map<String, String> m1 = new HashMap<>();
        m1.put("timestamp", "20221010-132801");
        m1.put("step", "1");
        m1.put("environment", "ProductionEnv");
        fixture.put("Google", new Metadata.Builder(url1).putAll(m1).build());
        //
        URL url2 = new URL("https://duckduckgo.com/");
        Map<String, String> m2 = new HashMap<>();
        m2.put("timestamp", "20221010-132806");
        m2.put("step", "2");
        m2.put("environment", "ProductionEnv");
        fixture.put("DuckDuckGo", new Metadata.Builder(url2).putAll(m2).build());
        //
        return fixture;
    }

    public static Map<String, Metadata> createFixtureRight() throws MalformedURLException {
        Map<String, Metadata> fixture = new HashMap<>();
        //
        URL url1 = new URL("https://www.google.com/");
        Map<String, String> m1 = new HashMap<>();
        m1.put("timestamp", "20221010-132801");
        m1.put("step", "1");
        m1.put("environment", "DevelopmentEnv");  // Here is a difference
        fixture.put("Google", new Metadata.Builder(url1).putAll(m1).build());
        //
        URL url2 = new URL("https://duckduckgo.com/");
        Map<String, String> m2 = new HashMap<>();
        m2.put("timestamp", "20221010-132806");
        m2.put("step", "2");
        m2.put("environment", "DevelopmentEnv");  // Here is a difference
        fixture.put("DuckDuckGo", new Metadata.Builder(url2).putAll(m2).build());
        //
        return fixture;
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        MaterialList left;
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "ProductionEnv");
        left = store.select(jobName, jobTimestamp1, QueryOnMetadata.builder(map1).build());
        //
        MaterialList right;
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("environment", "DevelopmentEnv");
        right = store.select(jobName, jobTimestamp2, QueryOnMetadata.builder(map2).build());
        //
        mpg =
                MaterialProductGroup.builder(left, right)
                        .ignoreKeys("URL.protocol", "URL.port").build();
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        SortKeys sortKeys = new SortKeys("step", "timestamp");
        mpg.order(sortKeys);
        String output = mpg.toVariableJson(sortKeys, true);
        JobTimestamp jt = JobTimestamp.laterThan(jobTimestamp2);
        store.write(jobName, jt, FileType.JSON, Metadata.NULL_OBJECT, JsonUtil.prettyPrint(output));
        System.out.println(JsonUtil.prettyPrint(output));
    }

}
