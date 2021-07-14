package com.kazurayam.materials.store


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class JobberTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(JobberTest.class.getName())

    private static Path imagesDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_images")

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void test_commit() {
        Path root = outputDir.resolve("Materials")
        Store repos = new Store(root)
        Jobber jobResult = repos.getJobber(new JobName("test_commit"), JobTimestamp.now())
        Metadata metadata = new Metadata("DevelopmentEnv", "http://demoaut-mimic.katalon.com/")
        BufferedImage image =  ImageIO.read(imagesDir.resolve("20210623_225337.development.png").toFile())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, FileType.PNG.getExtension(), baos);
        byte[] data = baos.toByteArray()
        jobResult.commit(metadata, data, FileType.PNG)
        //
    }
}
