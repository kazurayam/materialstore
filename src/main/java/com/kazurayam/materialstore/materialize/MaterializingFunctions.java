package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class MaterializingFunctions {

    /**
     * get HTML source of the target web page, pretty-print it, save it into
     * the store
     */
    public static MaterializingFunction<Target, WebDriver, StorageDirectory>
            storeHTMLSource = (target, driver, storageDirectory) -> {
        Objects.requireNonNull(target);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(storageDirectory);
        // open the page in browser
        driver.navigate().to(target.getUrl());
        // wait for the page to load completely
        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement handle =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        target.getBy()));
        //-------------------------------------------------------------
        // get the HTML source from browser
        String rawHtmlSource = driver.getPageSource();
        // pretty print HTML source
        Document doc = Jsoup.parse(rawHtmlSource, "", Parser.htmlParser());
        doc.outputSettings().indentAmount(2);
        String ppHtml = doc.toString();
        //-------------------------------------------------------------
        // write the HTML source into the store
        Metadata metadata = Metadata.builder(target.getUrl())
                .putAll(target.getParameters())
                .build();
        Store store = storageDirectory.getStore();
        JobName jobName = storageDirectory.getJobName();
        JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
        store.write(jobName, jobTimestamp, FileType.HTML, metadata, ppHtml);
    };

    /**
     *
     */
    public static MaterializingFunction<Target, WebDriver, StorageDirectory>
            storeEntirePageScreenshot = (target, driver, storageDirectory) -> {
        Objects.requireNonNull(target);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(storageDirectory);
        // open the page in browser
        driver.navigate().to(target.getUrl());
        // wait for the page to load completely
        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement handle =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        target.getBy()));
        //-------------------------------------------------------------
        int timeout = 500;  // milli-seconds
        // look up the device-pixel-ratio of the current machine
        JavascriptExecutor js = (JavascriptExecutor)driver;
        float dpr = (Long)js.executeScript("return window.devicePixelRatio;") * 1.0f;
        AShot aShot = new AShot()
                .coordsProvider(new WebDriverCoordsProvider())
                .shootingStrategy(ShootingStrategies.viewportPasting(
                                ShootingStrategies.scaling(dpr),
                                timeout));
        // take a screenshot of entire view of the page
        Screenshot screenshot = aShot.takeScreenshot(driver);
        BufferedImage bufferedImage = screenshot.getImage();
        //-------------------------------------------------------------
        // write the HTML source into the store
        Metadata metadata = Metadata.builder(target.getUrl())
                .putAll(target.getParameters())
                .build();
        Store store = storageDirectory.getStore();
        JobName jobName = storageDirectory.getJobName();
        JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, bufferedImage);
    };
}
