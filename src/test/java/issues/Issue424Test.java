package issues;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * reproduce the issue #424 as reported at
 * https://github.com/kazurayam/materialstore/issues/424
 */
public class Issue424Test {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(Issue424Test.class);
    private static final Path fixtureDir =
            too.getProjectDirectory().resolve("src/test/fixtures/issue#424");
    private static Store store;

    @BeforeAll
    public static void beforeAll() throws MaterialstoreException, IOException {
        Path root = too.cleanClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
    }

    /**
     * When either of the left or right Material is a NULL_OBJECT,
     * let's see how TextDifferToHTML.stuffDiff() goes?
     */
    @Test
    public void test_see_how_it_goes() throws MaterialstoreException {
        // prepare the fixture files
        JobName jobName = new JobName("test_see_how_it_goes");
        JobTimestamp jtLeft = JobTimestamp.now();
        JobTimestamp jtRight = JobTimestamp.laterThan(jtLeft);
        Metadata metadata =
                Metadata.builder().put("description", "fixture").build();
        Path csvFile = fixtureDir.resolve("c3f192874d9840021cb003059ea1664faead3d7c.csv");
        store.write(jobName, jtRight, FileType.CSV, metadata, csvFile);
        // invoke TextDifferToHTML.stuffDiff() to see how it goes
        MaterialList left = store.select(jobName, jtLeft, FileType.CSV, QueryOnMetadata.ANY);
        MaterialList right = store.select(jobName, jtRight, FileType.CSV, QueryOnMetadata.ANY);
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right).build();
        MaterialProduct stuffed = new TextDifferToHTML(store).stuffDiff(mpg.get(0));
        assertNotNull(stuffed);

        // in the "diff" directory, there should be a Material
        // with the Metadata of ["category":"NoMaterialFound"]
        JobTimestamp jtDiff = stuffed.getDiff().getJobTimestamp();
        MaterialList allMaterialsAsDiff = store.select(jobName, jtDiff);
        StringBuilder sb = new StringBuilder();
        allMaterialsAsDiff.forEach(material -> {
            sb.append(material.getIndexEntry().toString());
            sb.append("\n");
        });
        assertTrue(sb.toString().contains("NoMaterialFound"));
    }

}
