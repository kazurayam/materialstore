package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.core.TestHelper;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReducerTest {

    private static Store store;
    private static Store storeBackup;

    @BeforeAll
    public static void beforeAll() {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(ReducerTest.class);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
        storeBackup = Stores.newInstance(testClassOutputDir.resolve("store-backup"));
    }

    @BeforeEach
    public void setup() {}

    /*
     * the left is stuffed, the right is also stuffed
     */
    @Test
    public void test_chronos_both_stuffed() throws MaterialstoreException {
        JobName jobName = new JobName("test_chronos_both_stuffed");
        store.deleteJobName(jobName);
        JobTimestamp jt1 = JobTimestamp.now();
        writeRedApple(store, jobName, jt1);
        writeMikan(store, jobName, jt1);
        JobTimestamp jt2 = JobTimestamp.laterThan(jt1);
        writeGreenApple(store, jobName, jt2);
        writeMikan(store, jobName, jt2);
        //
        MaterialList currentMaterialList = store.select(jobName, jt2, QueryOnMetadata.ANY);
        MaterialProductGroup reduced = Reducer.chronos(store, currentMaterialList);
        //
        //System.out.println(reduced.toJson(true));
        assertNotNull(reduced);
        assertEquals(2, reduced.size());
    }

    @Test
    public void test_chronos_no_history() throws MaterialstoreException {
        JobName jobName = new JobName("test_chronos_no_previous");
        store.deleteJobName(jobName);
        // intentionally leave the left empty
        //JobTimestamp jt1 = JobTimestamp.now();
        //writeRedApple(store, jobName, jt1);
        //writeMikan(store, jobName, jt1);
        JobTimestamp jt2 = JobTimestamp.now();
        writeGreenApple(store, jobName, jt2);
        writeMikan(store, jobName, jt2);
        MaterialList currentMaterialList = store.select(jobName, jt2, QueryOnMetadata.ANY);
        MaterialProductGroup reduced = Reducer.chronos(store, currentMaterialList);
        //
        System.out.println(reduced.toJson(true));
        assertNotNull(reduced);
        assertEquals(2, reduced.size());
    }



    /*
     */
    private void writeRedApple(Store store, JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Path dir = TestHelper.getFixturesDirectory().resolve("apple_mikan_money");
        Path png = dir.resolve("03_apple.png");
        try {
            store.write(jobName, jobTimestamp, FileType.PNG,
                    Metadata.builder(png.toFile().toURI().toURL())
                            .put("description", "this is an apple")
                            .put("step", "01").build(),
                    png);
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }

    private void writeGreenApple(Store store, JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Path dir = TestHelper.getFixturesDirectory().resolve("apple_mikan_money");
        Path png = dir.resolve("06_green-apple.png");
        try {
            store.write(jobName, jobTimestamp, FileType.PNG,
                    Metadata.builder(png.toFile().toURI().toURL())
                            .put("description", "this is an apple")
                            .put("step", "01")
                            .build(),
                    png);
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }

    private void writeMikan(Store store, JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Path dir = TestHelper.getFixturesDirectory().resolve("apple_mikan_money");
        Path png = dir.resolve("04_mikan.png");
        try {
            store.write(jobName, jobTimestamp, FileType.PNG,
                    Metadata.builder(png.toFile().toURI().toURL())
                            .put("description", "this is an apple")
                            .put("step", "02")
                            .build(),
                    png);
        } catch (MalformedURLException e) {
            throw new MaterialstoreException(e);
        }
    }
}
