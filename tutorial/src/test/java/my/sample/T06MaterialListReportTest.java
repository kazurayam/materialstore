package my.sample;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class T06MaterialListReportTest {
    private Store store;
    @BeforeEach
    public void beforeEach() throws IOException {
        Path testClassOutputDir = TestHelper.createTestClassOutputDir(this);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test06_makeMaterialListReport() throws MaterialstoreException {
        JobName jobName =
                new JobName("test06_makeMaterialListReport()");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        SharedMethods.write3images(store, jobName, jobTimestamp);

        MaterialList materialList =
                store.select(jobName, jobTimestamp,
                        QueryOnMetadata.ANY);              // (18)

        Inspector inspector = Inspector.newInstance(store);
        inspector.setSortKeys(new SortKeys("step"));
        Path report = inspector.report(materialList);
        assertNotNull(report);
        System.out.println("report is found at " + report);
    }
}
