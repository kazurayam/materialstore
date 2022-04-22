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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Accepts a CSV text like
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
public class TargetCSVReader {

    private static Logger logger = LoggerFactory.getLogger(TargetCSVReader.class);

    public static List<Target> parse(String csvText) throws MaterialstoreException {
        Map<String, String> attributes = new LinkedHashMap<>();
        return parse(csvText, attributes);
    }

    public static List<Target> parse(String csvText, Map<String, String> attributes)
            throws MaterialstoreException {
        return parse(new StringReader(csvText), attributes);
    }

    public static List<Target> parse(Path csvPath) throws MaterialstoreException {
        Map<String, String> attributes = new LinkedHashMap<>();
        return parse(csvPath, attributes);
    }

    public static List<Target> parse(Path csvPath, Map<String, String> attributes)
            throws MaterialstoreException {
        return parse(csvPath.toFile(), attributes);
    }

    public static List<Target> parse(File csvPath) throws MaterialstoreException {
        Map<String, String> attributes = new LinkedHashMap<>();
        return parse(csvPath, attributes);
    }
    public static List<Target> parse(File csvPath, Map<String, String> attributes)
            throws MaterialstoreException {
        try {
            Reader reader = new InputStreamReader(
                    new FileInputStream(csvPath), StandardCharsets.UTF_8);
            return parse(reader, attributes);
        } catch (FileNotFoundException e) {
            throw new MaterialstoreException(e);
        }
    }

    public static List<Target> parse(Reader reader) throws MaterialstoreException {
        Map<String, String> attributes = new LinkedHashMap<>();
        return parse(reader, attributes);
    }

    public static List<Target> parse(Reader reader, Map<String, String> attributes)
            throws MaterialstoreException {
        List<Target> targetList = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String[] items = line.split(",");
                    if (items.length >= 2) {
                        // URL
                        URL url = new URL(items[0].trim());
                        String locator = items[1].trim();
                        // if the locator string starts with "/" then it must be a XPath
                        // otherwise a CSS Selector
                        By by = (locator.startsWith("/")) ?
                                By.xpath(locator) :
                                By.cssSelector(locator);
                        Target target =
                                Target.builder(url)
                                        .by(by)
                                        .putAll(attributes)
                                        .build();
                        targetList.add(target);
                    }
                }
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        return targetList;
    }

    private TargetCSVReader() {}
}
