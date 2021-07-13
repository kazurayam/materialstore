package com.kazurayam.taod

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class JobTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(OrganizerTest.class.getName())

    private static Path fixtureDir =
            Paths.get(".").resolve("src/test/resources/fixture")

    private Job job_

    @BeforeAll
    static void beforeAll() {
        Files.createDirectories(outputDir)
    }

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void test_commit() {
        Path root = outputDir.resolve(".taod")
        Organizer repos = new Organizer(root)
        Job job = repos.getJob(new JobName("test_commit"), JobTimestamp.now())
    }

    @Test
    void test_toString() {
        Path root = outputDir.resolve(".taod")
        Organizer repos = new Organizer(root)
        Job job = repos.getJob(new JobName("test_toString"), JobTimestamp.now())
        //println job.toString()
        assertTrue(job.toString().contains("\"jobName\":\"test_toString\""))
        assertTrue(job.toString().contains("\"jobTimestamp\":"))
        assertTrue(job.toString().contains("\"jobDir\":"))

    }
}
