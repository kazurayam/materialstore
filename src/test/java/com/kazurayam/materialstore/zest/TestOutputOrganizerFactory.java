package com.kazurayam.materialstore.zest;

import com.kazurayam.unittest.TestOutputOrganizer;

public class TestOutputOrganizerFactory {
    
    private TestOutputOrganizerFactory() {}

    public static TestOutputOrganizer create(Class<?> clazz) {
        return new TestOutputOrganizer.Builder(clazz)
                .outputDirPath("build/tmp/testOutput")
                .subDirPath(clazz)
                // e.g, "io.github.somebody.examples.SampleTest"
                .build();
    }
}
