package com.kazurayam.materialstore.zest;

import com.kazurayam.unittest.TestOutputOrganizer;

public class TestOutputOrganizerFactory {
    
    private TestOutputOrganizerFactory() {}

    public static TestOutputOrganizer create(Class<?> clazz) {
        return new TestOutputOrganizer.Builder(clazz)
                .outputDirectoryRelativeToProject("build/tmp/testOutput")
                .subOutputDirectory(clazz)
                // e.g, "io.github.somebody.examples.SampleTest"
                .build();
    }
}
