package com.kazurayam.materialstore.selenium

import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class AShotWrapperTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(AShotWrapperTest.class.getName())

    private static WebDriver driver;

    private int timeout = 150; // milli-seconds

    @BeforeAll
    static void beforeAll() {
        //
        if (Files.exists(outputDir)) {
            // delete the directory to clear out
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map {it.toFile() }
                    .forEach {it.delete() }
        }
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    void beforeEach(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(800, 800));
        driver.navigate().to("http://example.com");

    }

    @Test
    void test_takeWebElementImage() {
        BufferedImage image = AShotWrapper.takeWebElementImage(driver,
                By.xpath("//body/div"))
        assertNotNull(image)
        File screenshotFile = outputDir.resolve("test_takeWebElementImage.png").toFile()
        ImageIO.write(image, "PNG", screenshotFile)
        assertTrue(screenshotFile.exists())
    }

    @Test
    void test_takeEntirePageImage() {
        BufferedImage image = AShotWrapper.takeEntirePageImage(driver)
        assertNotNull(image)
        File screenshotFile = outputDir.resolve("test_takeEntirePageImage.png").toFile()
        ImageIO.write(image, "PNG", screenshotFile)
        assertTrue(screenshotFile.exists())
    }

    @Test
    void test_saveWebElementImage() {
        File screenshotFile = outputDir.resolve("test_saveWebElementImage.png").toFile()
        AShotWrapper.saveWebElementImage(driver,
                By.xpath("//body/div"), screenshotFile)
        assertTrue(screenshotFile.exists())
    }

    @Test
    void test_saveEntirePageImage() {
        File screenshotFile = outputDir.resolve("test_saveEntirePageImage.png").toFile()
        AShotWrapper.saveEntirePageImage(driver, screenshotFile)
        assertTrue(screenshotFile.exists())
    }

    @AfterEach
    void tearDown(){
        if (driver != null) {
            driver.quit();
        }
    }
}
