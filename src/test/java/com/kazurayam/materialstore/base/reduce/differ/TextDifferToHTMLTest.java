package com.kazurayam.materialstore.base.reduce.differ;

import static org.junit.jupiter.api.Assertions.*;


import com.kazurayam.chrome4testing.ChromeForTestingDriverFactory;
import com.kazurayam.chrome4testing.Installation;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.base.report.AbstractReporterTest;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.StoreImpl;
import com.kazurayam.materialstore.zest.SampleFixtureInjector;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Test the generating diff HTML,
 * verify the HTML content (split-diff and unified-diff) using Selenium WebDriver
 */
public class TextDifferToHTMLTest extends AbstractReporterTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TextDifferToHTMLTest.class);
    private static Store store;

    private WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path root = too.cleanClassOutputDirectory().resolve("store");
        store = new StoreImpl(root);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        // specify the path of Chrome for Testing and chromedriver binaries
        ChromeForTestingDriverFactory df =
                new ChromeForTestingDriverFactory(Installation.mac_136_0_7103_113_mac_x64);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"}); // to disable "Chrome is being controlled by automated test software"
        options.addArguments("--test-type=gpu"); // to disable "Chrome for Testing is only for automated testing. For regular browsing, use a standard version of Chrome that updates automatically"
        // open a browser window
        driver = df.newChromeForTestingDriver(options);
    }

    @AfterEach
    public void afterEach() {
        driver.quit();
    }

    @Test
    public void test_sideBySideDiff() throws MaterialstoreException, IOException {
        Path htmlFile = generateDiffHTML("test_splitDiff");
        driver.get(htmlFile.toUri().toURL().toExternalForm());
        //
        assertTrue(driver.findElement(By.xpath("//table[@id='side-by-side']")).isDisplayed(),
                "missing <table id='split-diff'>");
    }

    @Test
    public void test_unifiedDiff() throws MaterialstoreException, MalformedURLException {
        Path htmlFile = generateDiffHTML("test_unifiedDiff");
        driver.get(htmlFile.toUri().toURL().toExternalForm());
        // click the radio button to switch the diff format from Side-by-side to Unified
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@name='diffFormat' and @value='unified']"))
                ).click();
        // assert that `<table id='unified'>` is visible
        assertTrue(driver.findElement(By.xpath("//table[@id='unified']")).isDisplayed(),
                    "missing <table id='unified-diff'>");
    }

    private static Path generateDiffHTML(String jobNameString) throws MaterialstoreException {
        // Prepare
        JobName jobName = new JobName(jobNameString);
        SampleFixtureInjector.injectSampleResults(store, jobName);
        MaterialProduct materialProductToDiff = prepareMaterialProductToDiff(store, jobName);
        TextDifferToHTML textDiffer = new TextDifferToHTML(store);
        textDiffer.enablePrettyPrinting(false);
        // When
        MaterialProduct materialProductAsDiff = textDiffer.generateDiff(materialProductToDiff);
        // Then
        assertNotNull(materialProductAsDiff);
        // Serialize
        Material diffMaterial = materialProductAsDiff.getDiff();
        Path htmlFile = store.getRoot().resolve(jobName.toString()).resolve("diff.html");
        long length = store.export(diffMaterial, htmlFile);
        return htmlFile;
    }

    /**
     *
     */
    private static MaterialProduct prepareMaterialProductToDiff(Store store, JobName jobName) throws MaterialstoreException {
        MaterialProductGroup reducedMPG = prepareMPG(store, jobName);
        MaterialProduct mp = reducedMPG.get(0);
        assert mp != null;
        return mp;
    }

    /**
     */
    private static MaterialProductGroup prepareMPG(Store store, JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("category", "page source");
        map.put("environment", "ProductionEnv");
        MaterialList expected = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map).build());
        Assertions.assertEquals(1, expected.size());

        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("category", "page source");
        map1.put("environment", "DevelopmentEnv");
        MaterialList actual = store.select(jobName, jobTimestamp, FileType.HTML, QueryOnMetadata.builder(map1).build());
        Assertions.assertEquals(1, actual.size());

        MaterialProductGroup reducedMPG = MaterialProductGroup.builder(expected, actual).ignoreKeys("environment", "URL.host").build();
        Assertions.assertNotNull(reducedMPG);
        Assertions.assertEquals(1, reducedMPG.size());
        return reducedMPG;
    }
}
