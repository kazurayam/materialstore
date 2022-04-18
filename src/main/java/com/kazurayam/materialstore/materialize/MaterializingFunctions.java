package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class MaterializingFunctions {

    public static MaterializingFunction<TargetURL, WebDriver, StorageDirectory>
            saveHTMLSource = (targetURL, driver, storageDirectory) -> {
        Objects.requireNonNull(targetURL);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(storageDirectory);
        // open the page in browser
        driver.navigate().to(targetURL.getUrl());
        // get the HTML source from browser
        String rawHtmlSource = driver.getPageSource();
        // pretty print HTML source
        Document doc = Jsoup.parse(rawHtmlSource, "", Parser.htmlParser());
        doc.outputSettings().indentAmount(2);
        String ppHtml = doc.toString();
        // write the HTML source into the store
        Metadata metadata = Metadata.builder(targetURL.getUrl()).build();
        Store store = storageDirectory.getStore();
        JobName jobName = storageDirectory.getJobName();
        JobTimestamp jobTimestamp = storageDirectory.getJobTimestamp();
        store.write(jobName, jobTimestamp, FileType.HTML, metadata, ppHtml);
    };

}
