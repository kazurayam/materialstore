package com.kazurayam.materialstore.demo

import com.kazurayam.materialstore.diff.DifferDriverImpl
import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.DifferDriver

import com.kazurayam.materialstore.selenium.AShotWrapper
import com.kazurayam.materialstore.store.*
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class VisualTestingTwins {

    private Path root_ = Paths.get("./build/tmp/demoOutput/${VisualTestingTwins.class.getSimpleName()}/Materials")

    void setRoot(Path root) {
        this.root_ = root
    }

    void init() {
        if (Files.exists(root_)) {
            // delete the directory to clear out
            Files.walk(root_)
                    .sorted(Comparator.reverseOrder())
                    .map {it.toFile() }
                    .forEach {it.delete() }
        }
        Files.createDirectories(root_)
    }

    static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(120, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(800, 800));
        return driver
    }

    void execute() {
        init()
        Store store = Stores.newInstance(root_)
        JobName jobName = new JobName("VisualTestingTwins")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        // open the Chrome browser
        WebDriver driver = createChromeDriver()

        // visit the 1st page
        String profile1 = "ProductionEnv"
        URL url1 = new URL("http://demoaut.katalon.com/")
        Tuple result1 = doAction(driver, store, jobName, jobTimestamp, profile1, url1)
        assert result1 != null

        // visit the 2nd page
        String profile2 = "DevelopmentEnv"
        URL url2 = new URL("http://demoaut-mimic.kazurayam.com/")
        Tuple result2 = doAction(driver, store, jobName, jobTimestamp, profile2, url2)
        assert result2 != null

        // close the Chrome browser
        driver.quit()

        // pickup the screenshots that belongs to the 2 "profiles", make image-diff files of each.
        List<Material> screenshotsOfProfile1 = store.select(jobName, jobTimestamp,
                FileType.PNG, new MetadataPattern([ "profile": profile1 ]))
        assert screenshotsOfProfile1 != null

        List<Material> screenshotsOfProfile2 = store.select(jobName, jobTimestamp,
                FileType.PNG, new MetadataPattern([ "profile": profile2 ]))
        assert screenshotsOfProfile2 != null

        //
        List<DiffArtifact> artifactsToDiff =
                store.zipMaterials(
                        screenshotsOfProfile1,
                        screenshotsOfProfile2,
                        ["URL.file"] as Set)
        assert artifactsToDiff != null

        // makes diff images, save them into "objects" dir with updated "index".
        // returns the list of DiffArtifact which have the diff property stuffed.
        DifferDriver differDriver = new DifferDriverImpl.Builder().root(root_).build()
        List<DiffArtifact> artifactsWithDiff= differDriver.makeDiff(artifactsToDiff)
        assert artifactsWithDiff != null
        assert artifactsWithDiff.size() > 0

        /*
        // compile HTML report
        Reporter reporter = store.newReporter(jobName, jobTimestamp)
        Path reportFile = store.getRoot().resolve("index.html")
        reporter.report(artifactsWithDiff, reportFile)
         */
    }

    private static Tuple doAction(WebDriver driver,
                           Store store, JobName jobName, JobTimestamp jobTimestamp,
                           String profile, URL url) {
        // visit the page
        driver.navigate().to(url.toString())

        /*
         * Metadata([
         *     "profile":"ProductionEnv",
         *     "URL": "http://demoaut.katalon.com/",
         *     "URL.host": "demoaut.katalon.com",
         *     "URL.file": "/"
         * ])
         */
        Metadata metadata = new Metadata([
                "category": "screenshot",
                "profile": profile,
                "URL": url.toExternalForm(),
                "URL.host": url.getHost(),
                "URL.file": url.getFile()
        ])

        // take and store the PNG screenshot of the page
        BufferedImage image = AShotWrapper.takeEntirePageImage(driver)
        Material imageMaterial = store.write(jobName, jobTimestamp, FileType.PNG, metadata, image)
        assert imageMaterial != null

        // get and store the HTML page source of the page
        String html = driver.getPageSource()
        metadata["category"] = "page source"
        Material htmlMaterial = store.write(jobName, jobTimestamp, FileType.HTML, metadata,
                html, StandardCharsets.UTF_8)
        assert htmlMaterial != null

        return new Tuple(imageMaterial, htmlMaterial)
    }

    static void main(String[] args) {
        try {
            VisualTestingTwins instance = new VisualTestingTwins()
            instance.execute()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
