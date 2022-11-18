package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DotGeneratorTest {

    private static final Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(DotGeneratorTest.class.getName());

    private static final Path issue259Dir =
            Paths.get(".")
                    .resolve("src/test/fixtures/issue#259");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftTimestamp;
    private static JobTimestamp rightTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        FileUtils.copyDirectory(issue259Dir.resolve("store").toFile(), store.getRoot().toFile());
        jobName = new JobName("Main_Twins");
        leftTimestamp = new JobTimestamp("20220522_094639");
        rightTimestamp = new JobTimestamp("20220522_094706");
    }

    //@Disabled
    @Test
    public void test_generateDot_Material() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220522_094639");
        Material material = store.selectSingle(jobName, fixtureTimestamp, FileType.PNG);
        //
        String dotText = DotGenerator.generateDot(material, true);
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        //
        JobName outJobName = new JobName("test_generateDot_Material");
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

    //@Disabled
    @Test
    public void test_generateDot_MaterialList() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220522_094639");
        MaterialList materialList = store.select(jobName, fixtureTimestamp);
        //
        String dotText = DotGenerator.generateDot(materialList);
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        //
        JobName outJobName = new JobName("test_generateDot_MaterialList");
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

    //@Disabled
    @Test
    public void test_generateDot_MaterialProduct() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_generateDot_MaterialProduct");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProduct mp =
                new MaterialProduct.Builder(
                        leftMaterialList.get(0),
                        rightMaterialList.get(0),
                        jobName,
                        reducedTimestamp)
                        .build();
        //
        String dotText = DotGenerator.generateDot(mp);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        //
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }

    //@Disabled
    @Test
    public void test_generateDotOfMPGBeforeZip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_generateDotOfMPGBeforeZip");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("environment", "URL.host").build();
        //
        String dotText = DotGenerator.generateDotOfMPGBeforeZip(mProductGroup);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        //
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }

    //@Disabled
    @Test
    public void test_generateDot_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        // save the dot file and the PNG image into the store directory
        JobName outJobName = new JobName("test_generateDot_MProductGroup");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup reduced =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList)
                        .ignoreKeys("environment", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap("URL.query", "\\w{32}")
                        )
                        .build();
        assert reduced.size() > 0;
        //
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);

        // generate a dot file
        String dotText = DotGenerator.generateDot(inspected);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toFile(store).length() > 0);
        // generate the image of the MProductGroup object by Graphviz
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toFile(store).length() > 0);
    }
}
