package tut.motivation;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import tut.util.ScreenshotUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Selenium-Java based Junit5 Test, which does
 * 1. open Chrome browser window, navigate to a URL
 * 2. take a screenshot of the page using AShot
 * 3. create a local directory
 * 4. name a PNG file with some descriptive name
 * 5. write the screenshot image into the file
 *
 * Problem:
 * I can not name the file equal to the URL given because I can not use
 * special special characters: '/', ':' which comprises a URL string.
 *
 */
public class TakeScreenshotAndSaveIntoFileTest {

    private static String URL_STR = "http://myadmin.kazurayam.com";
    private WebDriver driver = null;
    private static Path outputDir;

    @BeforeAll
    public static void beforeAll() throws IOException {
        // create the output directory
        outputDir =
                Paths.get(System.getProperty("user.dir"))
                        .resolve("build/tmp/tutOutput")
                        .resolve(TakeScreenshotAndSaveIntoFileTest.class.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        // initialize the setting of Chrome Driver
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void beforeEach() {
        ChromeOptions option=new ChromeOptions();
        option.addArguments("headless");
        option.addArguments("window-size=1200x600");
        driver = new ChromeDriver(option);
    }

    @Test
    public void test_takeScreenshotAndSaveIntoFile() throws IOException {
        driver.navigate().to(URL_STR);
        BufferedImage im = ScreenshotUtil.takeEntirePageImage(driver);
        // determine the path of output file, ensure directory tree
        String subDir = "test_takeScreenshotAndSaveIntoFile";
        String fileName = "demoaut-mimic_katalon_com.png";
        Path png = outputDir.resolve(subDir).resolve(fileName);
        Files.createDirectories(png.getParent());
        // write the image into file
        ImageIO.write(im, "png", png.toFile());
        //
        assertTrue(Files.exists(png));
        assertTrue(png.toFile().length() > 0);
    }


    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterAll
    public static void afterAll() {}
}
