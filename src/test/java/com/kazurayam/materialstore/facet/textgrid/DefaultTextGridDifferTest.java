package com.kazurayam.materialstore.facet.textgrid;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTextGridDifferTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(DefaultTextGridDifferTest.class);
    private final List<List<String>> input1 = new ArrayList<List<String>>() {{
        add(Arrays.asList("Customer A", "Retail B", "x"));
        add(Arrays.asList("Customer C", "Retail B", "x"));
        add(Arrays.asList("Customer D", "Key Account", "x"));
        add(Arrays.asList("Customer E", "Retail A", "x"));
        add(Arrays.asList("Customer F", "Key Account", "x"));
        add(Arrays.asList("Customer G", "Key Account", "x"));
    }};

    private Path outputDir;

    private final List<List<String>> input2 = new ArrayList<List<String>>() {{
        add(Arrays.asList("Customer A", "Retail B", "x"));
        add(Arrays.asList("Customer D", "Key Account", "x"));
        add(Arrays.asList("Customer B", "Retail A", "x"));
        add(Arrays.asList("Customer C", "Retail B", "x"));
        add(Arrays.asList("Customer E", "Retail A", "x"));
        add(Arrays.asList("Customer G", "Key Account", "x"));
    }};

    @BeforeEach
    public void setup() throws IOException {
        outputDir = too.cleanClassOutputDirectory();
    }

    @Test
    public void test_diffTextGrids_typical() throws MaterialstoreException {
        DefaultTextGridDiffer differ = new DefaultTextGridDiffer(outputDir);
        int warnings = differ.diffTextGrids(input1, input2, new KeyRange(0, 1), "SampleCase");
        System.out.println("the report is found at " +
                differ.getReportPathRelativeTo(too.getProjectDirectory()));
        Assertions.assertTrue(warnings > 0);
    }
}
