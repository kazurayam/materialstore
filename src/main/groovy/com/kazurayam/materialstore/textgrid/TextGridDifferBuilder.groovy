package com.kazurayam.materialstore.textgrid

import com.kazurayam.materialstore.MaterialstoreFacade

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.MetadataPattern
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The "Builder" pattern of GOF is employed
 *
 */
abstract class TextGridDifferBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TextGridDifferBuilder.class)

    private Path projectDir

    private Path reportFile

    TextGridDifferBuilder() {
        this(Paths.get(System.getProperty("user.dir")))
    }

    TextGridDifferBuilder(Path projectDir) {
        Objects.requireNonNull(projectDir)
        this.projectDir = projectDir
        reportFile = null
    }

    /*
     *
     */
    int diffTextGrids(List<List<String>> input1, List<List<String>> input2,
                      String givenJobName) {
        diffTextGrids(input1, input2, 0..0, givenJobName)
    }

    int diffTextGrids(List<List<String>> input1, List<List<String>> input2,
                      Range<Integer> keyRange, String givenJobName) {
        Path root = projectDir.resolve("store")
        Store store = Stores.newInstance(root)
        JobName jobName = new JobName(givenJobName)

        JobTimestamp timestamp1 = JobTimestamp.now()
        jsonifyAndStore(store, jobName, timestamp1, input1, keyRange, "input1")

        Thread.sleep(1000)

        JobTimestamp timestamp2 = JobTimestamp.now()
        jsonifyAndStore(store, jobName, timestamp2, input2, keyRange, "input2")

        MaterialList left = store.select(jobName, timestamp1,
                new MetadataPattern.Builder().build())

        MaterialList right = store.select(jobName, timestamp2,
                new MetadataPattern.Builder().build())
        double criteria = 0.0d

        MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)

        ArtifactGroup preparedDAG =
                ArtifactGroup.builder(left, right)
                        .ignoreKeys("input")
                        .build()
        ArtifactGroup stuffedDAG = facade.workOn(preparedDAG)

        int warnings = stuffedDAG.countWarnings(criteria)

        reportFile =
                facade.reportArtifactGroup(jobName, stuffedDAG, criteria,
                        jobName.toString() + "-index.html")
        assert Files.exists(reportFile)
        logger.info("report is found at " + reportFile.normalize().toAbsolutePath())

        return warnings
    }

    /*
     *
     */
    private final void jsonifyAndStore(Store store,
                                       JobName jobName, JobTimestamp jobTimestamp,
                                       List<List<String>> input, Range<Integer> keyRange,
                                       String inputId) {
        jsonifyAndStoreRows(store, jobName, jobTimestamp, input, keyRange, inputId)
        jsonifyAndStoreKeys(store, jobName, jobTimestamp, input, keyRange, inputId)
    }

    /*
     *
     */
    abstract void jsonifyAndStoreRows(
            Store store, JobName jobName, JobTimestamp jobTimestamp,
            List<List<String>> input, Range<Integer> keyRange, String inputId)

    /*
     *
     */
    abstract void jsonifyAndStoreKeys(
            Store store, JobName jobName, JobTimestamp jobTimestamp,
            List<List<String>> input, Range<Integer> keyRange, String inputId)


    /*
     *
     */
    Path getReportPath() {
        return reportFile.normalize().toAbsolutePath()
    }

    Path getReportPathRelativeTo(Path base) {
        return base.relativize(getReportPath())
    }

    // ---------- helpers

    protected static final void writeLinesIntoFile(List<String> lines, File file) {
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file),"UTF-8")))
        for (String line in lines) {
            pw.println(line)
        }
        pw.flush()
        pw.close()
    }

}
