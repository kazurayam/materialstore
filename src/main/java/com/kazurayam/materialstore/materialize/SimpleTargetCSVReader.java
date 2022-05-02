package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Accepts a CSV text like
 *
 * <PRE>
 * http://www.google.com/,//input[@id="q"]
 * http://example.com/,//a[starts-with("More information...")]
 * </PRE>
 * to return a List of Target objects.
 *
 * The 1st column is interpreted as a URL string.
 * The 2nd column is interpreted as an XPath string to a web element in a web page.
 * The system will wait for the web element to be visible after the URL is loaded into browser.
 */
public final class SimpleTargetCSVReader extends TargetCSVReader {

    private static Logger logger = LoggerFactory.getLogger(SimpleTargetCSVReader.class);

    SimpleTargetCSVReader() {}

    @Override
    public List<Target> parse(String csvText) throws MaterialstoreException {
        return parse(new StringReader(csvText));
    }

    @Override
    public List<Target> parse(Path csvPath) throws MaterialstoreException {
        return parse(csvPath.toFile());
    }

    @Override
    public List<Target> parse(File csvFile) throws MaterialstoreException {
        try {
            Reader reader = new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8);
            return parse(reader);
        }  catch (FileNotFoundException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public List<Target> parse(Reader reader) throws MaterialstoreException {
        List<Target> targetList = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String[] items = line.split(",");
                    URL url = new URL(items[0].trim());
                    Target target;
                    if (items.length >= 2) {
                        String locator = items[1].trim();
                        // if the locator string starts with "/" then it must be a XPath
                        // otherwise a CSS Selector
                        By by = (locator.startsWith("/")) ?
                                By.xpath(locator) :
                                By.cssSelector(locator);
                        target = Target.builder(url)
                                .by(by)
                                .build();
                    } else {
                        target = Target.builder(url).build();
                    }
                    targetList.add(target);
                }
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        return targetList;
    }

}
