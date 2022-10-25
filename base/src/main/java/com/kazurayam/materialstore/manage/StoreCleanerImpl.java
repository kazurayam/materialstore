package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialLocator;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreCleanerImpl extends StoreCleaner {

    private final Store store;

    public StoreCleanerImpl(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public void cleanup(JobName jobName) throws IOException, MaterialstoreException {
        Objects.requireNonNull(jobName);
        Set<JobTimestamp> markedJT = new HashSet<>();
        JobTimestamp latestJobTimestamp = store.findLatestJobTimestamp(jobName);
        markedJT.add(latestJobTimestamp);
        // as for Chronos mode, retain a few more JobTimestamps that are
        // referred by the latest JobTimestamp
        for (Material m : store.select(jobName, latestJobTimestamp)) {
            if (m.getMetadata().containsKey("category") &&
                    m.getMetadata().get("category").equals("diff")) {
                MaterialLocator leftLocator =
                        MaterialLocator.parse(m.getMetadata().get("left"));
                markedJT.add(leftLocator.getJobTimestamp());
                MaterialLocator rightLocator =
                        MaterialLocator.parse(m.getMetadata().get("right"));
                markedJT.add(rightLocator.getJobTimestamp());
            }
        }
        // delete unnecessary JobTimestamps
        for (JobTimestamp jt : store.findAllJobTimestamps(jobName)) {
            if (!markedJT.contains(jt)) {
                store.deleteJobTimestamp(jobName, jt);
            }
        }
        // delete older reports other than the latest
        this.deleteReportsOlderThan(jobName, latestJobTimestamp);
    }



    @Override
    public int deleteJobTimestampsOlderThan(JobName jobName,
                                            JobTimestamp thanThisJobTimestamp)
            throws MaterialstoreException, IOException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(thanThisJobTimestamp);

        // identify the JobTimestamp directories to be deleted
        List<JobTimestamp> toBeDeleted = store.findAllJobTimestampsPriorTo(jobName, thanThisJobTimestamp);
        // now delete files/directories
        int countDeletedJT = 0;
        for (JobTimestamp jt : toBeDeleted) {
            store.deleteJobTimestamp(jobName, jt);
            countDeletedJT += 1;
        }
        return countDeletedJT;
    }

    @Override
    public int deleteReportsOlderThan(JobName jobName, JobTimestamp jobTimestamp) throws IOException {
        // identify "store/<JobName>-<JobTimestamp>.html" files to be deleted
        List<Path> reports = findAllReportsOf(jobName);
        int count = 0;
        for (Path report : reports) {
            String baseFileName =
                    store.resolveReportFileName(jobName, jobTimestamp);
            if (report.getFileName().toString().compareTo(baseFileName) < 0) {
                try {
                    Files.delete(report);
                    count += 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return count;
    }

    @Override
    public List<Path> findAllReportsOf(JobName jobName) throws IOException {
        try (Stream<Path> stream = Files.list(store.getRoot())) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().startsWith(jobName.toString()))
                    .filter(p -> p.getFileName().toString().endsWith(".html"))
                    .collect(Collectors.toList());
        }
    }

}
