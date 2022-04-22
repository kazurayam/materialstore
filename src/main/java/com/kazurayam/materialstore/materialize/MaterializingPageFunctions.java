package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class MaterializingPageFunctions {

    /**
     * get HTML source of the target web page, pretty-print it, save it into
     * the store
     */
    public static MaterializingPageFunction<Target, WebDriver, StorageDirectory, Material>
            storeHTMLSource = (target, driver, storageDirectory) -> {
        Objects.requireNonNull(target);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(storageDirectory);
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
                .putAll(target.getAttributes())
                .build();
        Store store = storageDirectory.getStore();
        JobName jobName = storageDirectory.getJobName();
        JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
        Material material = store.write(jobName, jobTimestamp, FileType.HTML, metadata, ppHtml);
        return material;
    };

    /**
     *
     */
    public static MaterializingPageFunction<Target, WebDriver, StorageDirectory, Material>
            storeEntirePageScreenshot = (target, driver, storageDirectory) -> {
        Objects.requireNonNull(target);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(storageDirectory);
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
        // scroll the view to the top of the page
        js.executeScript("window.scrollTo(0, 0);");
        //-------------------------------------------------------------
        // write the PNG image into the store
        Metadata metadata = Metadata.builder(target.getUrl())
                .putAll(target.getAttributes())
                .build();
        Store store = storageDirectory.getStore();
        JobName jobName = storageDirectory.getJobName();
        JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
        Material material = store.write(jobName, jobTimestamp, FileType.PNG, metadata, bufferedImage);
        return material;
    };

    private MaterializingPageFunctions() {}

}
