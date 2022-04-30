package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DotGeneratorTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(DotGeneratorTest.class.getName());

    private static final Path issue80Dir =
            Paths.get(".")
                    .resolve("src/test/fixture/issue#80");

    private static Store store;
    private static JobName jobName;
    JobTimestamp leftTimestamp = new JobTimestamp("20220128_191320");
    JobTimestamp rightTimestamp = new JobTimestamp("20220128_191342");

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        FileUtils.copyDirectory(issue80Dir.toFile(), store.getRoot().toFile());
        jobName = new JobName("MyAdmin_visual_inspection_twins");
    }

    @Test
    public void test_generateDOT_Material() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220128_191320");
        Material material = store.selectSingle(jobName, fixtureTimestamp, FileType.PNG);
        Map<String, String> options = Collections.emptyMap();
        //
        String dotText = DotGenerator.toDot(material, options, true);
        BufferedImage bufferedImage = DotGenerator.toDiagram(dotText);
        //
        JobName outJobName = new JobName("test_generateDOT_Material");
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }


    @Test
    public void test_generateDOT_MaterialList() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220128_191320");
        MaterialList materialList = store.select(jobName, fixtureTimestamp);
        //
        String dotText = DotGenerator.toDot(materialList);
        BufferedImage bufferedImage = DotGenerator.toDiagram(dotText);
        //
        JobName outJobName = new JobName("test_generateDOT_MaterialList");
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }


    @Test
    public void test_generateDOT_MaterialProduct() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        MaterialProduct mp =
                new MaterialProduct.Builder(
                        leftMaterialList.get(0),
                        rightMaterialList.get(0),
                        reducedTimestamp).build();
        //
        String dotText = DotGenerator.toDot(mp);
        BufferedImage bufferedImage = DotGenerator.toDiagram(dotText);
        //
        JobName outJobName = new JobName("test_generateDOT_MaterialProduct");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }


    @Test
    public void test_generateDOT_MProductGroupBuilder() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        MProductGroup.Builder mpgb =
                new MProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList);
        //
        String dotText = DotGenerator.toDot(mpgb);
        BufferedImage bufferedImage = DotGenerator.toDiagram(dotText);
        //
        JobName outJobName = new JobName("test_generateDOT_MaterialProduct");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }


    @Test
    public void
    test_generateDOT_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        MProductGroup mProductGroup =
                new MProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("profile", "URL.host").build();
        //
        String dotText = DotGenerator.toDot(mProductGroup);
        BufferedImage bufferedImage = DotGenerator.toDiagram(dotText);
        //
        JobName outJobName = new JobName("test_generateDOT_MProductGroup");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }
}
