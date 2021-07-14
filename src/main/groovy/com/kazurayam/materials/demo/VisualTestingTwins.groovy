package com.kazurayam.materials.demo

import com.kazurayam.materials.selenium.AShotWrapper
import com.kazurayam.materials.store.FileType
import com.kazurayam.materials.store.JobName
import com.kazurayam.materials.store.JobTimestamp
import com.kazurayam.materials.store.Material
import com.kazurayam.materials.store.Metadata
import com.kazurayam.materials.store.Store
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

import java.awt.image.BufferedImage
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

    WebDriver createChromeDriver() {
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
        Store store = new Store(root_)
        JobName jobName = new JobName("VisualTestingTwins")
        JobTimestamp jobTimestamp = JobTimestamp.now()
        // open the Chrome browser
        WebDriver driver = createChromeDriver()
        // visit the 1st page
        URL url1 = new URL("http://demoaut.katalon.com/")
        doAction(store, jobName, jobTimestamp, driver, url1, "ProductionEnv")
        // visit the 2nd page
        URL url2 = new URL("http://demoaut-mimic.kazurayam.com/")
        doAction(store, jobName, jobTimestamp, driver, url2, "DevelopmentEnv")
        // close the Chrome browser
        driver.quit()
        // query for image pairs, take diff, and compile a HTML reportgi
    }

    private void doAction(Store store, JobName jobName, JobTimestamp jobTimestamp,
                          WebDriver driver, URL url, String profile) {
        // visit the page
        driver.navigate().to(url.toString())
        Metadata metadata2 = new Metadata(profile, driver.getCurrentUrl())

        // take and store screenshot of the page
        BufferedImage image = AShotWrapper.takeEntirePageImage(driver)
        Material mateG = store.write(jobName, jobTimestamp, metadata2,
                image, FileType.PNG)
        assert mateG != null

        // get and store the HTML page source of the page
        String html = driver.getPageSource()
        Material mateH = store.write(
                jobName, jobTimestamp, metadata2, html, FileType.HTML)
        assert mateH != null
    }

    static void main(String[] args) {
        VisualTestingTwins instance = new VisualTestingTwins()
        instance.execute()
    }
}
