package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterializingPageFunctionsTest {

    private static Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterializingPageFunctionsTest.class.getName());

    private static Store store;
    private WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void beforeEach() {
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("headless");
        driver = new ChromeDriver(opt);
        driver.manage().window().setSize(new Dimension(1024, 768));
    }

    @Test
    void test_storeHTMLSource() throws MaterialstoreException {
        Target target = new Target.Builder("https://www.google.com")
                .by(By.cssSelector("input[name=\"q\"]"))
                .build();
        JobName jobName = new JobName("test_storeHTMLSource");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory = new StorageDirectory(store, jobName, jobTimestamp);
        // open the page in browser
        driver.navigate().to(target.getUrl());
        // get HTML source of the page, save it into the store
        Material createdMaterial  = MaterializingPageFunctions.storeHTMLSource.accept(target, driver, storageDirectory);
        assertNotNull(createdMaterial);
        // assert that a material has been created
        Material selectedMaterial = store.selectSingle(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.ANY);
        assertTrue(Files.exists(selectedMaterial.toPath(store.getRoot())));
        assertEquals(createdMaterial, selectedMaterial);
    }

    @Test
    void test_storeEntirePageScreenshot() throws MaterialstoreException {
        Target target = new Target.Builder("https://github.com/kazurayam")
                .by(By.cssSelector("main#js-pjax-container"))
                .build();
        JobName jobName = new JobName("test_storeEntirePageScreenshot");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory = new StorageDirectory(store, jobName, jobTimestamp);
        // open the page in browser
        driver.navigate().to(target.getUrl());
        // take an entire page screenshot, write the image into the store
        Material createdMaterial = MaterializingPageFunctions.storeEntirePageScreenshot.accept(target, driver, storageDirectory);
        assertNotNull(createdMaterial);
        // assert that a material has been created
        Material selectedMaterial = store.selectSingle(jobName, jobTimestamp, FileType.PNG, QueryOnMetadata.ANY);
        assertTrue(Files.exists(selectedMaterial.toPath(store.getRoot())));
        assertEquals(createdMaterial, selectedMaterial);
    }


    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
