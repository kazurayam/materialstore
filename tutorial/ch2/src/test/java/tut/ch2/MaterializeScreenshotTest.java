package tut.ch2;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.inspector.Inspector;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import tut.util.ScreenshotUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterializeScreenshotTest {
    private static final String URL_STR = "http://myadmin.kazurayam.com";
    private static Store store;
    private WebDriver driver = null;

    @BeforeAll
    public static void beforeAll() throws IOException {
        // initialize the setting of Chrome Driver
        WebDriverManager.chromedriver().setup();
        // create the output directory
        Path outputDir =
                Paths.get(System.getProperty("user.dir"))
                        .resolve("build/tmp/tutOutput")
                        .resolve(MaterializeScreenshotTest.class.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Path root = outputDir.resolve("store");
        Files.createDirectories(root);

        // create the Store object and its directory
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void beforeEach() {
        ChromeOptions option=new ChromeOptions();
        option.addArguments("headless");
        option.addArguments("window-size=1200x600");
        driver = new ChromeDriver(option);
    }

    @Test
    public void checkPage() throws MalformedURLException, MaterialstoreException {
        // let browser navigate to the target URL
        URL url = new URL(URL_STR);
        driver.navigate().to(url);

        // determine the directory tree
        JobName jobName = new JobName("sampleJob");
        JobTimestamp jobTimestamp = JobTimestamp.now();

        // create a metadata to associate the object file
        String description1 = "entire page screenshot";
        Metadata metadata1 =
                Metadata.builder(url)
                        .put("description", description1)
                        .build();

        // take a screenshot of the entire page
        BufferedImage im = ScreenshotUtil.takeEntirePageImage(driver);

        // write the image into a new object within the store directory
        Material m1 = store.write(jobName, jobTimestamp, FileType.PNG, metadata1, im);

        assertTrue(Files.exists(m1.toPath(store)));         // assert that the file exists
        assertTrue(m1.toFile(store).length() > 0); // assert that the file is not empty

        // a Material object can remember the source URL from which the screenshot was taken
        assertEquals(URL_STR, m1.getMetadata().toURLAsString());  // -> "http://myadmin.kazurayam.com"

        // a Material object can remember any associated metadata
        assertEquals(description1, m1.getMetadata().get("description")); // -> "entire page screenshot"

        //-------------------------------------------------------------

        // get the HTML source of the rendered page
        String htmlSource = driver.getPageSource();

        // create a metadata to associate the object file
        String description2 = "HTML source of the page";
        Metadata metadata2 =
                Metadata.builder(url).put("description", description2)
                        .build();

        // write the html text into a new object within the store directory
        Material m2 = store.write(jobName, jobTimestamp, FileType.HTML, metadata2, htmlSource);

        assertTrue(Files.exists(m2.toPath(store)));
        assertTrue(m2.toFile(store).length() > 0);

        // a Material object can remember the source URL from which the screenshot was taken
        assertEquals(URL_STR, m2.getMetadata().toURLAsString());

        // a Material object can remember any associated metadata
        assertEquals(description2, m2.getMetadata().get("description"));

        //-------------------------------------------------------------

        // additional feature:
        // create a report of the stored Material objects
        MaterialList materialList = store.select(jobName, jobTimestamp);
        JobTimestamp reportTimestamp = JobTimestamp.laterThan(jobTimestamp);
        Inspector inspector = Inspector.newInstance(store);
        Path report = inspector.report(materialList, "stored_materials.html");
        assertTrue(Files.exists(report));
        System.out.println("find the list of Material objects at " + report.toString());
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
        }
    }
}
