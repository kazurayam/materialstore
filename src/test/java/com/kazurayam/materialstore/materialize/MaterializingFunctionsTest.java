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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterializingFunctionsTest {

    private static Path outputDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("build/tmp/testOutput")
                    .resolve(MaterializingFunctionsTest.class.getName());

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
        driver = new ChromeDriver();
    }


    @Test
    void test_saveHTMLSource() throws MaterialstoreException {
        TargetURL targetURL = new TargetURL.Builder("https://www.google.com")
                .locatorType(LocatorType.CSS_SELECTOR)
                .locator("input[name=\"q\"]")
                .build();
        JobName jobName = new JobName("test_saveHTMLSource");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory = new StorageDirectory(store, jobName, jobTimestamp);
        MaterializingFunctions.saveHTMLSource.accept(targetURL, driver, storageDirectory);
        Material material = store.selectSingle(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.ANY);
        assertNotNull(material);
        assertTrue(Files.exists(material.toPath(store.getRoot())));
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
