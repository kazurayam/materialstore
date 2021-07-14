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

        // 1st screenshot
        driver.navigate().to("http://demoaut.katalon.com/")
        BufferedImage image1 = AShotWrapper.takeEntirePageImage(driver)
        Metadata metadata1 = new Metadata("ProductionEnv", driver.getCurrentUrl())
        Material expected = store.write(jobName, jobTimestamp, metadata1, image1, FileType.PNG)
        assert expected != null

        // 2nd screenshot
        driver.navigate().to("http://demoaut-mimic.kazurayam.com")
        BufferedImage image2 = AShotWrapper.takeEntirePageImage(driver)
        Metadata metadata2 = new Metadata("DevelopmentEnv", driver.getCurrentUrl())
        Material actual = store.write(jobName, jobTimestamp, metadata2, image2, FileType.PNG)
        assert actual != null

        // close the Chrome browser
        driver.quit()

        // query for image pairs, take diff, and compile a HTML report
    }

    static void main(String[] args) {
        VisualTestingTwins instance = new VisualTestingTwins()
        instance.execute()
    }
}
