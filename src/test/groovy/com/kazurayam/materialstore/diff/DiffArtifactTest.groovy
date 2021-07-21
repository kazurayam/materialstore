package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.diff.differ.ImageDifferToPNG
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.Store
import com.kazurayam.materialstore.store.StoreImpl
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DiffArtifactTest {

    @Test
    void test_getDescription() {
        MetadataPattern mp = new MetadataPattern([
                "URL.host": "demoaut-mimic.kazurayam.com",
                "URL.file": "/"
        ])
        DiffArtifact diffArtifact =
                new DiffArtifact(
                        Material.NULL_OBJECT,
                        Material.NULL_OBJECT,
                        mp)
        assertEquals(
                '''{"URL.file":"/","URL.host":"demoaut-mimic.kazurayam.com"}''',
                diffArtifact.getDescription())
    }

}
