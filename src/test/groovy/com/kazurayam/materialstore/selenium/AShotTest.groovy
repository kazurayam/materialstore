package com.kazurayam.materialstore.selenium

import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import ru.yandex.qatools.ashot.AShot
import ru.yandex.qatools.ashot.Screenshot
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider
import ru.yandex.qatools.ashot.shooting.ShootingStrategies

import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * - visit a web site "http://demoaut.katalon.core"
 * - take 2 screenshots of a web element '<a id="menu-toggle"></a>'
 * - once with the view port size width = 400
 * - another with the view port size width maximum
 * - 2 images will be similar but actually different for 40% pixel-size
 * - compare 2 images to generate a diff image
 * - save 3 PNG files
 * - compile a HTML view of the 3 images
 * - throw failure when the diff ratio > 10%, otherwise pass the test.
 * - using Selenium WebDriver with Chrome Browser
 * - using AShot library
 */
class AShotTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(AShotTest.class.getName())

    private static WebDriver driver;

    private int timeout = 150; // milli-seconds

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            // delete the directory to clear out
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map {it.toFile() }
                    .forEach {it.delete() }
        }
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    void test_takeScreenshot()
    {
        assert driver != null;
        driver.manage().window().setSize(new Dimension(800, 800));
        driver.navigate().to("http://example.com")
        WebElement content = driver.findElement(By.xpath('//body/div'))
        // take the screenshot of a <div> element
        float dpr = DevicePixelRatioResolver.resolveDPR(driver)
        Screenshot screenshot = new AShot()
                .coordsProvider(new WebDriverCoordsProvider()) //find coordinates with WebDriver API
                .shootingStrategy(ShootingStrategies.viewportPasting(ShootingStrategies.scaling(dpr), timeout))
                .takeScreenshot(driver, content);
        assertNotNull(screenshot)
        Path screenshotFile = outputDir.resolve("screenshot.png")
        ImageIO.write(screenshot.getImage(), "PNG", screenshotFile.toFile())
        assertTrue(Files.exists(screenshotFile))
    }

    @AfterEach
    void tearDown(){
        if (driver != null) {
            driver.quit();
        }
    }

}
