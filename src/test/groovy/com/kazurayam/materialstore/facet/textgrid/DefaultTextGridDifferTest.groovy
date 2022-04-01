package com.kazurayam.materialstore.facet.textgrid


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertTrue

class DefaultTextGridDifferTest {

    List<List<String>> input1 = [
            [ "Customer A", "Retail B", "x" ],
            // [ "Customer B", "Retail A", "x" ],
            [ "Customer C", "Retail B", "x" ],
            [ "Customer D", "Key Account", "x" ],
            [ "Customer E", "Retail A", "x" ],
            [ "Customer F", "Key Account", "x" ],
            [ "Customer G", "Key Account", "x" ],
    ]

    List<List<String>> input2 = [
            [ "Customer A", "Retail B" , "x"],
            [ "Customer D", "Key Account" , "x"],
            [ "Customer B", "Retail A" , "x"],
            [ "Customer C", "Retail B" , "x"],
            [ "Customer E", "Retail A" , "x"],
            // [ "Customer F", "Key Account" , "x"],
            [ "Customer G", "Key Account" , "x"]
    ]

    Path projectDir
    Path outputDir

    @BeforeEach
    void setup() {
        projectDir = Paths.get(System.getProperty("user.dir"))
        outputDir = projectDir.resolve("build/tmp/testOutput")
                        .resolve(this.getClass().getSimpleName())
    }

    @Test
    void test_diffTextGrids_typical() {
        DefaultTextGridDiffer differ = new DefaultTextGridDiffer(outputDir)
        int warnings = differ.diffTextGrids(input1, input2, new KeyRange(0,1), "SampleCase")
        Path report = differ.getReportPath()
        println "the report is found at " + differ.getReportPathRelativeTo(projectDir)
        assertTrue(warnings > 0)
    }
}