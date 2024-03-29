package com.kazurayam.materialstore.facet.textgrid;

import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.core.FileSystemFactory;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * The "Builder" pattern of GOF is employed
 */
public abstract class TextGridDifferBuilder {

    public TextGridDifferBuilder() throws IOException {
        this(FileSystemFactory.newFileSystem().getPath(System.getProperty("user.dir")));
    }

    public TextGridDifferBuilder(Path projectDir) {
        Objects.requireNonNull(projectDir);
        this.projectDir = projectDir;
        reportFile = null;
    }


    public int diffTextGrids(List<List<String>> input1, List<List<String>> input2, KeyRange keyRange, String givenJobName) throws MaterialstoreException {
        Path root = projectDir.resolve("store");
        Store store = Stores.newInstance(root);
        JobName jobName = new JobName(givenJobName);

        JobTimestamp timestamp1 = JobTimestamp.now();
        jsonifyAndStore(store, jobName, timestamp1, input1, keyRange, "input1");

        JobTimestamp timestamp2 = JobTimestamp.laterThan(timestamp1);
        jsonifyAndStore(store, jobName, timestamp2, input2, keyRange, "input2");

        MaterialList left = store.select(jobName, timestamp1, QueryOnMetadata.builder().build());

        MaterialList right = store.select(jobName, timestamp2, QueryOnMetadata.builder().build());
        double threshold = 0.0d;

        Inspector inspector = Inspector.newInstance(store);

        MaterialProductGroup reduced = MaterialProductGroup.builder(left, right).ignoreKeys("input").build();
        MaterialProductGroup processed = inspector.reduceAndSort(reduced);
        int warnings = processed.countWarnings(threshold);
        reportFile = inspector.report(processed, threshold);
        assert Files.exists(reportFile);
        logger.info("report is found at " + reportFile.normalize().toAbsolutePath());

        return warnings;
    }

    private final void jsonifyAndStore(Store store,
                                       JobName jobName,
                                       JobTimestamp jobTimestamp,
                                       List<List<String>> input,
                                       KeyRange keyRange,
                                       String inputId) throws MaterialstoreException {
        jsonifyAndStoreRows(store, jobName, jobTimestamp, input, keyRange, inputId);
        jsonifyAndStoreKeys(store, jobName, jobTimestamp, input, keyRange, inputId);
    }

    public abstract void jsonifyAndStoreRows(Store store,
                                             JobName jobName,
                                             JobTimestamp jobTimestamp,
                                             List<List<String>> input,
                                             KeyRange keyRange,
                                             String inputId) throws MaterialstoreException;

    public abstract void jsonifyAndStoreKeys(Store store,
                                             JobName jobName,
                                             JobTimestamp jobTimestamp,
                                             List<List<String>> input,
                                             KeyRange keyRange,
                                             String inputId) throws MaterialstoreException;

    public Path getReportPath() {
        return reportFile.normalize().toAbsolutePath();
    }

    public Path getReportPathRelativeTo(Path base) {
        return base.relativize(getReportPath());
    }

    protected static void writeLinesIntoFile(List<String> lines, File file) throws MaterialstoreException {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
            for (String line : lines) {
                pw.println(line);
            }

            pw.flush();
            pw.close();
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TextGridDifferBuilder.class);
    private Path projectDir;
    private Path reportFile;
}
