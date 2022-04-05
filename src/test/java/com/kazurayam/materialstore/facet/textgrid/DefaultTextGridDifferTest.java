package com.kazurayam.materialstore.facet.textgrid;

import com.kazurayam.materialstore.MaterialstoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTextGridDifferTest {

    private final List<List<String>> input1 = new ArrayList<List<String>>() {{
        add(Arrays.asList("Customer A", "Retail B", "x"));
        add(Arrays.asList("Customer C", "Retail B", "x"));
        add(Arrays.asList("Customer D", "Key Account", "x"));
        add(Arrays.asList("Customer E", "Retail A", "x"));
        add(Arrays.asList("Customer F", "Key Account", "x"));
        add(Arrays.asList("Customer G", "Key Account", "x"));
    }};

    private final List<List<String>> input2 = new ArrayList<List<String>>() {{
        add(Arrays.asList("Customer A", "Retail B", "x"));
        add(Arrays.asList("Customer D", "Key Account", "x"));
        add(Arrays.asList("Customer B", "Retail A", "x"));
        add(Arrays.asList("Customer C", "Retail B", "x"));
        add(Arrays.asList("Customer E", "Retail A", "x"));
        add(Arrays.asList("Customer G", "Key Account", "x"));
    }};

    private Path projectDir;
    private Path outputDir;

    @BeforeEach
    public void setup() {
        projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput").resolve(this.getClass().getSimpleName());
    }

    @Test
    public void test_diffTextGrids_typical() throws MaterialstoreException {
        DefaultTextGridDiffer differ = new DefaultTextGridDiffer(outputDir);
        int warnings = differ.diffTextGrids(input1, input2, new KeyRange(0, 1), "SampleCase");
        System.out.println("the report is found at " + differ.getReportPathRelativeTo(projectDir));
        Assertions.assertTrue(warnings > 0);
    }

}
