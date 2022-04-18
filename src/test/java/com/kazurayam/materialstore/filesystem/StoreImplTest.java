package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.TestFixtureUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StoreImplTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(StoreImplTest.class.getName());
    private static final Path imagesDir = Paths.get(".").resolve("src/test/fixture/sample_images");
    private static final Path resultsDir = Paths.get(".").resolve("src/test/fixture/sample_results");
    private static final Path htmlDir = Paths.get(".").resolve("src/test/fixture/sample_html");
    private static final Boolean verbose = true;
    private Path root;
    private Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        // if verbose logging required, change the log level
        if (verbose) {
            System.setProperty("org.slf4j.simpleLogger.log.com.kazurayam.materialstore.filesystem.StoreImpl", "DEBUG");
        }

    }

    @BeforeEach
    public void setup() {
        root = outputDir.resolve("store");
        store = new StoreImpl(root);
    }

    @Test
    public void test_copyMaterials() throws MaterialstoreException {
        JobName jobName = new JobName("test_copyMaterials");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp source = new JobTimestamp("20210715_145922");
        JobTimestamp target = JobTimestamp.now();
        store.copyMaterials(jobName, source, target);
        //
        MaterialList copied = store.select(jobName, target, QueryOnMetadata.ANY);
        Assertions.assertTrue(copied.size() > 0);
    }

    @Test
    public void test_deleteMaterialsOlderThanExclusive() throws MaterialstoreException {
        JobName jobName = new JobName("test_deleteMaterialsOlderThanExclusive");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp latestTimestamp = new JobTimestamp("20210715_145922");
        int deletedJobTimestamps = store.deleteMaterialsOlderThanExclusive(jobName, latestTimestamp, 0L, ChronoUnit.DAYS);
        assertEquals(1, deletedJobTimestamps);
        /* 1 JobTimestamp directory is deleted
         * - 20210713_093357/
         * under which contained 3 files plus 1 directory
         * - 20210713_093357/objects/12a1a5e ...
         * - 20210713_093357/objects/6141b60 ...
         * - 20210713_093357/objects/ab56d30 ...
         * - 20210713_093357/objects/
         * - 20210713_093357/index
         */
    }

    @Test
    public void test_findAllJobTimestamps() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("test_findAllJobTimestamps");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        List<JobTimestamp> jobTimestamps = store.findAllJobTimestamps(jobName);
        Assertions.assertNotNull(jobTimestamps);
        assertEquals(3, jobTimestamps.size());
        assertEquals(new JobTimestamp("20210715_150000"), jobTimestamps.get(0));
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamps.get(1));
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(2));
    }

    @Test
    public void test_findAllJobTimestampsPriorTo() throws MaterialstoreException {
        JobName jobName = new JobName("test_findAllJobTimestampsPriorTo");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_150000");
        List<JobTimestamp> jobTimestamps = store.findAllJobTimestampsPriorTo(jobName, jobTimestamp);
        Assertions.assertNotNull(jobTimestamps);
        assertEquals(2, jobTimestamps.size());
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamps.get(0));
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(1));
    }

    @Test
    public void test_findLatestJobTimestampPriorTo() throws MaterialstoreException {
        JobName jobName = new JobName("test_findLatestJobTimestampPriorTo");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp latest = store.findLatestJobTimestamp(jobName);// 20210715_150000
        JobTimestamp second = store.findJobTimestampPriorTo(jobName, latest);// 20210715_145922
        Assertions.assertNotNull(second);
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, second);
        assertEquals(new JobTimestamp("20210715_145922"), second);
    }

    @Test
    public void test_findLatestJobTimestamp() throws MaterialstoreException {
        JobName jobName = new JobName("test_findLatestJobTimestamp");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        JobTimestamp jobTimestamp = store.findLatestJobTimestamp(jobName);
        Assertions.assertNotNull(jobTimestamp);
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, jobTimestamp);
        assertEquals(new JobTimestamp("20210715_150000"), jobTimestamp);
    }

    /**
     * Test if the Job object cache mechanism in the Organizer object works
     */
    @Test
    public void test_getCachedJob() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("test_getCachedJob");
        JobTimestamp jobTimestamp = new JobTimestamp("20210713_093357");
        // make sure the Job directory to be empty
        FileUtils.deleteDirectory(root.resolve(jobName.toString()).toFile());
        // null should be returned if the Job directory is not present or empty
        Jobber expectedNull = store.getCachedJobber(jobName, jobTimestamp);
        Assertions.assertNull(expectedNull, "expected null but was not");
        // stuff the Job directory with a fixture
        Path jobNameDir = root.resolve(jobName.toString());
        FileUtils.copyDirectory(resultsDir.toFile(), jobNameDir.toFile());
        // new Job object should be created by calling the getJob() method
        Jobber newlyCreatedJob = store.getJobber(jobName, jobTimestamp);
        Assertions.assertNotNull(newlyCreatedJob, "should not be null");
        // a Job object should be returned from the cache by the getCachedJob() method
        Jobber cachedJob = store.getCachedJobber(jobName, jobTimestamp);
        Assertions.assertNotNull(cachedJob, "expected non-null but was null");
    }

    @Test
    public void test_getJobResult() throws MaterialstoreException {
        JobName jobName = new JobName("test_getJob");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Jobber job = store.getJobber(jobName, jobTimestamp);
        Assertions.assertNotNull(job);
        assertEquals("test_getJob", job.getJobName().toString());
    }

    @Test
    public void test_getPathOf() throws MaterialstoreException {
        JobName jobName = new JobName("test_getAbsolutePathOf");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        Path abs = store.getPathOf(materialList.get(0));
        Assertions.assertNotNull(abs);
        System.out.println(abs);
    }

    @Test
    public void test_getRoot() {
        Assertions.assertTrue(Files.exists(store.getRoot()), getRoot() + " is not present");
        assertEquals("store", store.getRoot().getFileName().toString());
    }

    @Test
    public void test_queryAllJobTimestamp() throws MaterialstoreException {
        JobName jobName = new JobName("test_queryAllJobTimestamps");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("profile", "DevelopmentEnv");
        Metadata metadata = new Metadata.Builder().putAll(map).build();
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build();
        List<JobTimestamp> jobTimestamps = store.queryAllJobTimestamps(jobName, query);
        Assertions.assertNotNull(jobTimestamps);
        assertEquals(3, jobTimestamps.size());
        assertEquals(new JobTimestamp("20210715_150000"), jobTimestamps.get(0));
        assertEquals(new JobTimestamp("20210715_145922"), jobTimestamps.get(1));
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(2));
    }

    @Test
    public void test_queryAllJobTimestampPriorTo() throws MaterialstoreException {
        JobName jobName = new JobName("test_queryAllJobTimestampsPriorTo");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("profile", "DevelopmentEnv");
        Metadata metadata = new Metadata.Builder().putAll(map).build();
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build();
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        List<JobTimestamp> jobTimestamps = store.queryAllJobTimestampsPriorTo(jobName, query, jobTimestamp);
        Assertions.assertNotNull(jobTimestamps);
        assertEquals(1, jobTimestamps.size());
        assertEquals(new JobTimestamp("20210713_093357"), jobTimestamps.get(0));
    }

    @Test
    public void test_queryJobTimestampPriorTo() throws MaterialstoreException {
        JobName jobName = new JobName("test_queryJobTimestampPriorTo");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("profile", "DevelopmentEnv");
        Metadata metadata = new Metadata.Builder().putAll(map).build();
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build();
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_150000");
        JobTimestamp found = store.queryJobTimestampPriorTo(jobName, query, jobTimestamp);
        Assertions.assertNotNull(found);
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, found);
        assertEquals(new JobTimestamp("20210715_145922"), found);
    }

    @Test
    public void test_reflect_without_JobTimestamp() throws MaterialstoreException {
        JobName jobName = new JobName("test_reflect_without_JobTimestamp");
        TestFixtureUtil.setupFixture(store, jobName);
        // add one more JobTimestamp directory to mee the test requirement
        store.copyMaterials(jobName, new JobTimestamp("20210713_093357"), new JobTimestamp("20210715_145947"));

        // create the base MaterialList of FileType.HTML
        MaterialList base = store.select(jobName, new JobTimestamp("20210715_150000"), FileType.HTML, QueryOnMetadata.ANY);

        // now reflect the base to find a target MaterialList of FileType.HTML
        // out of some previous JobTimestamp directory
        MaterialList target = store.reflect(base);

        assertEquals(new JobTimestamp("20210715_145922"), target.getJobTimestamp());
        assertEquals(2, target.size());
        assertEquals(FileType.HTML, target.get(0).getFileType());
        assertEquals(FileType.HTML, target.get(1).getFileType());
    }

    @Test
    public void test_reflect_with_JobTimestamp() throws MaterialstoreException {
        JobName jobName = new JobName("test_reflect_with_JobTimestamp");
        TestFixtureUtil.setupFixture(store, jobName);
        // add one more JobTimestamp directory to mee the test requirement
        store.copyMaterials(jobName, new JobTimestamp("20210715_145922"), new JobTimestamp("20210613_150000"));
        //
        JobTimestamp beginningOfTheMonth = new JobTimestamp("20210715_150000").beginningOfTheMonth();
        assertEquals(new JobTimestamp("20210701_000000"), beginningOfTheMonth);

        // create the base MaterialList of FileType.HTML
        MaterialList base = store.select(jobName, new JobTimestamp("20210715_150000"), FileType.HTML, QueryOnMetadata.ANY);

        // now reflect the base to find a target MaterialList of FileType.HTML
        // out of some JobTimestamp directory prior to the end of the last month
        MaterialList target = store.reflect(base, beginningOfTheMonth);

        assertEquals(new JobTimestamp("20210613_150000"), target.getJobTimestamp());
    }

        @Test
    public void test_retrieve() throws MaterialstoreException {
        JobName jobName = new JobName("test_retrieve");
        TestFixtureUtil.setupFixture(store, jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        MaterialList materialList = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        Assertions.assertNotNull(materialList, "material is null");
        Assertions.assertTrue(materialList.size() > 0);
        Path out = store.getRoot().resolve(jobName.toString()).resolve("screenshot.png");
        // now do retrieve
        store.retrieve(materialList.get(0), out);
        Assertions.assertTrue(Files.exists(out));
    }

    @Test
    public void test_queryLatestJobTimestamp() throws MaterialstoreException {
        JobName jobName = new JobName("test_queryLatestJobTimestamp");
        TestFixtureUtil.setupFixture(store, jobName);
        //
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("profile", "DevelopmentEnv");
        Metadata metadata = new Metadata.Builder().putAll(map).build();
        QueryOnMetadata query = new QueryOnMetadata.Builder(metadata).build();
        JobTimestamp found = store.queryLatestJobTimestamp(jobName, query);
        Assertions.assertNotNull(found);
        Assertions.assertNotEquals(JobTimestamp.NULL_OBJECT, found);
        assertEquals(new JobTimestamp("20210715_150000"), found);
    }

    @Test
    public void test_read() throws MaterialstoreException {
        JobName jobName = new JobName("test_read");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        //
        byte[] bytes = store.read(material);
        Assertions.assertTrue(bytes.length > 0);
    }

    @Test
    public void test_readAllLines() throws MaterialstoreException {
        JobName jobName = new JobName("test_readAllLines");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://devadmin.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = htmlDir.resolve("development.html");
        Material material = store.write(jobName, jobTimestamp, FileType.HTML, metadata, input);
        //
        List<String> allLines = store.readAllLines(material, StandardCharsets.UTF_8);
        Assertions.assertTrue(allLines.size() > 0);
        /*
        allLines.eachWithIndex({line, index ->
            println(index + ":" + line)
        })
         */
    }

    @Test
    public void test_select_2_files_in_4() throws MaterialstoreException {
        final JobName jobName = new JobName("test_select_2_files_in_4");
        final JobTimestamp jobTimestamp = JobTimestamp.now();
        final Path input = imagesDir.resolve("20210710_142631.development.png");
        //
        List<Metadata> metadataList = new ArrayList<>();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("city", "Thanh pho Ho Chi Minh");
        map.put("country", "VN");
        metadataList.add(Metadata.builder(map).build());
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("city", "Tokyo");
        map1.put("country", "JP");
        metadataList.add(Metadata.builder(map1).build());
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(2);
        map2.put("city", "Prague");
        map2.put("country", "CZ");
        metadataList.add(Metadata.builder(map2).build());
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(2);
        map3.put("city", "Toronto");
        map3.put("country", "CA");
        metadataList.add(Metadata.builder(map3).build());
        metadataList.forEach(metadata -> {
            Material material = null;
            try {
                material =
                        getStore().write(jobName, jobTimestamp,
                                FileType.PNG, metadata,
                                input);
            } catch (MaterialstoreException e) {
                e.printStackTrace();
            }
            Assertions.assertNotNull(material);
        });
        // select 2 members amongst 4 by matching "city" with a Regular Expression `To.*`
        MaterialList selected = store.select(jobName, jobTimestamp,
                QueryOnMetadata.builder()
                        .put("city", Pattern.compile("To.*"))
                        .build());
        assertEquals(2, selected.size());
    }

    @Test
    public void test_select_with_QueryOnMetadataAny_FileTypePNG() throws MaterialstoreException {
        JobName jobName = new JobName("test_select_with_QueryOnMetadataAny_FileTypePNG");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        // insert NON-PNG file
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("profile", "DevelopmentEnv");
        map1.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadataMore = Metadata.builder(map1).build();
        Path inputMore = htmlDir.resolve("development.html");
        Material materialMore = store.write(jobName, jobTimestamp, FileType.HTML, metadataMore, inputMore);
        Assertions.assertNotNull(materialMore);
        // select specifying FileType.PNG excluding FileType.HTML and others
        MaterialList materials = store.select(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        Assertions.assertNotNull(materials);
        assertEquals(1, materials.size());
    }

    @Test
    public void test_select_without_QueryOnMetadataAny_without_FileType() throws MaterialstoreException {
        JobName jobName = new JobName("test_select_without_QueryOnMetadataAny_without_FileType");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        //
        // insert NON-PNG file
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("profile", "DevelopmentEnv");
        map1.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadataMore = Metadata.builder(map1).build();
        Path inputMore = htmlDir.resolve("development.html");
        Material materialMore = store.write(jobName, jobTimestamp, FileType.HTML, metadataMore, inputMore);
        Assertions.assertNotNull(materialMore);
        // select all
        MaterialList materials = store.select(jobName, jobTimestamp);
        Assertions.assertNotNull(materials);
        assertEquals(2, materials.size());
    }

    @Test
    public void test_selectSingle_generic() throws MaterialstoreException {
        JobName jobName = new JobName("test_selectSingle_generic");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        //
        QueryOnMetadata query = QueryOnMetadata.builder().put("profile", Pattern.compile(".*")).put("URL", Pattern.compile(".*")).build();
        // select specifying FileType
        Material mat = store.selectSingle(jobName, jobTimestamp, FileType.PNG, query);
        Assertions.assertNotNull(mat);
        Assertions.assertTrue(Files.exists(mat.toPath(store.getRoot())));
    }

    @Test
    public void test_selectSingle_specific() throws MaterialstoreException, MalformedURLException {
        JobName jobName = new JobName("test_selectSingle_specific");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder(new URL("http://demoaut-mimic.kazurayam.com/")).put("profile", "DevelopmentEnv").build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build();
        // select
        Material mat = store.selectSingle(jobName, jobTimestamp, FileType.PNG, query);
        Assertions.assertNotNull(mat);
        Assertions.assertTrue(Files.exists(mat.toPath(store.getRoot())));
    }

    @Test
    public void test_write_BufferedImage() throws IOException, MaterialstoreException {
        JobName jobName = new JobName("test_write_BufferedImage");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProductionEnv");
        map.put("URL", "http://demoaut.katalon.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142628.production.png");
        BufferedImage image = ImageIO.read(input.toFile());
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, image);
        Assertions.assertNotNull(material);
        Assertions.assertTrue(ID.isValid(material.getIndexEntry().getID().toString()));
    }

    @Test
    public void test_write_File() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_file");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        File input = imagesDir.resolve("20210710_142631.development.png").toFile();
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        Assertions.assertTrue(ID.isValid(material.getIndexEntry().getID().toString()));
    }

    @Test
    public void test_write_Path() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_path");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "DevelopmentEnv");
        map.put("URL", "http://demoaut-mimic.kazurayam.com/");
        Metadata metadata = Metadata.builder(map).build();
        Path input = imagesDir.resolve("20210710_142631.development.png");
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, input);
        Assertions.assertNotNull(material);
        Assertions.assertTrue(ID.isValid(material.getIndexEntry().getID().toString()));
    }

    @Test
    public void test_write_string() throws MaterialstoreException {
        JobName jobName = new JobName("test_write_String");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("profile", "ProductionEnv");
        map.put("URL", "http://demoaut.katalon.com/");
        Metadata metadata = Metadata.builder(map).build();
        String input = "犬も歩けば棒に当たる";
        Material material = store.write(jobName, jobTimestamp, FileType.TXT, metadata, input);
        Assertions.assertNotNull(material);
        Assertions.assertTrue(ID.isValid(material.getIndexEntry().getID().toString()));
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

}
