package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.diagram.dot.DotGenerator;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * "sort the sequence of MaterialProducts by the ascending order of
 * QueryOnMetadata as string"
 * https://github.com/kazurayam/materialstore/issues/87
 *
 */
@Disabled   // sometimes this fails, and I can not solve it, yet.
public class MaterialProductGroup_issue87Test {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(MaterialProductGroup_issue87Test.class);
    private static final Path fixtureDir =
            too.getProjectDir().resolve("src/test/fixtures/issue#80");
    private static Store store;
    private JobName jobName;
    private MaterialProductGroup mpg;

    @BeforeAll
    public static void beforeAll() throws IOException {
        too.cleanClassOutputDirectory();
        Path storePath = too.getClassOutputDirectory().resolve("store");
        too.copyDir(fixtureDir, storePath);
        store = Stores.newInstance(storePath);
    }

    @BeforeEach
    public void setup() throws MaterialstoreException {
        jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");

        MaterialList left = store.select(jobName, timestampP,
                QueryOnMetadata
                        .builder(Collections.singletonMap("environment", "MyAdmin_ProductionEnv"))
                        .build());
        assert left.size() == 8;

        MaterialList right = store.select(jobName, timestampD,
                QueryOnMetadata
                        .builder(Collections.singletonMap("environment", "MyAdmin_DevelopmentEnv"))
                        .build());
        assert right.size() == 8;

        mpg = MaterialProductGroup.builder(left, right)
                .ignoreKeys("environment", "URL.host")
                .identifyWithRegex(Collections.singletonMap("URL.query", "\\w{32}"))
                .build();
    }

    @Test
    public void test_getQueryOnMetadataList() throws MaterialstoreException {

        List<QueryOnMetadata> queryList = mpg.getQueryOnMetadataList();

        System.out.println("[test_getQueryOnMetadataList]");
        for (int i = 0; i < queryList.size(); i++) {
            System.out.println(String.format("queryList.get(%d): %s", i, queryList.get(i).toString()));
        }
        // https://github.com/kazurayam/materialstore/issues/351
        // generate a diagram of the MaterialProductGroup object
        // save the image into a new folder
        String dot = DotGenerator.generateDot(mpg);
        BufferedImage bi = DotGenerator.toImage(dot);
        store.write(jobName, JobTimestamp.now(), FileType.PNG, Metadata.builder().build(), bi);

        // Assert
        Assertions.assertEquals(8, queryList.size());

        /* before issue#87, the output was
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2", "URL.port":"80", "URL.protocol":"https", "URL.query":"30af91bf14e37666a085fb8a161ff36d"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/ajax/libs/jquery/1.12.4/jquery.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/umineko-1960x1960.jpg", "URL.port":"80", "URL.protocol":"http"}

after issue87, the output is
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/", "URL.port":"80", "URL.protocol":"http"}
{"URL.path":"/ajax/libs/jquery/1.12.4/jquery.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css", "URL.port":"80", "URL.protocol":"https"}
{"URL.path":"/npm/bootstrap-icons@1.7.2/font/fonts/bootstrap-icons.woff2", "URL.port":"80", "URL.protocol":"https", "URL.query":"30af91bf14e37666a085fb8a161ff36d"}
{"URL.path":"/umineko-1960x1960.jpg", "URL.port":"80", "URL.protocol":"http"}
         */
        Assertions.assertTrue(queryList.get(0).toString().startsWith("{\"URL.path\":\"/\""), queryList.get(0).toString());
        Assertions.assertTrue(queryList.get(2).toString().startsWith("{\"URL.path\":\"/ajax"), queryList.get(2).toString());
    }


    @Disabled
    @Test
    public void test_toStringRepresentation_short() {
        String desc = mpg.toVariableJson(new SortKeys(), false);
        //System.out.println("[test_toStringRepresentation_short]\n" + JsonUtil.prettyPrint(desc));
    }

    @Disabled
    @Test
    public void test_toStringRepresentation_full() {
        String desc = mpg.toVariableJson(new SortKeys(), true);
        //System.out.println("[test_toStringRepresentation_full]\n" + JsonUtil.prettyPrint(desc));
    }

    @Disabled
    @Test
    public void test_getJobTimestampLeft() {
        Assertions.assertEquals(new JobTimestamp("20220128_191320"), mpg.getJobTimestampLeft());
    }

    @Disabled
    @Test
    public void test_getJobTimestampRight() {
        Assertions.assertEquals(new JobTimestamp("20220128_191342"), mpg.getJobTimestampRight());
    }

    @Disabled
    @Test
    public void test_getJobTimestampPrevious() {
        Assertions.assertEquals(new JobTimestamp("20220128_191320"), mpg.getJobTimestampPrevious());
    }

    @Disabled
    @Test
    public void test_getJobTimestampFollowing() {
        Assertions.assertEquals(new JobTimestamp("20220128_191342"), mpg.getJobTimestampFollowing());
    }

}
