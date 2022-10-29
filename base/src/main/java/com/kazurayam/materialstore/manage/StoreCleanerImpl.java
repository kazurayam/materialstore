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
import java.util.Collections;
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
    public void cleanup(JobName jobName) throws MaterialstoreException {
        List<JobTimestamp> diffs = store.findDifferentiatingJobTimestamps(jobName);
        if (diffs.size() > 0) {
            this.cleanup(jobName, diffs.get(0));
        } else {
            this.doCleanup(jobName, store.findLatestJobTimestamp(jobName));
        }
    }

    @Override
    public void cleanup(JobName jobName, JobTimestamp olderThan) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(olderThan);
        List<JobTimestamp> diffs = store.findDifferentiatingJobTimestamps(jobName);
        if (diffs.size() > 0) {
            diffs.sort(Collections.reverseOrder());
            JobTimestamp boundary = store.findLatestJobTimestamp(jobName);
            for (JobTimestamp jt : diffs) {
                if (jt.compareTo(olderThan) >= 0) {
                    boundary = jt;
                }
            }
            this.doCleanup(jobName, boundary);
        } else {
            List<JobTimestamp> nonDiffs =
                    store.findAllJobTimestampsPriorTo(jobName, olderThan);
            if (nonDiffs.size() > 0) {
                this.doCleanup(jobName, nonDiffs.get(nonDiffs.size()));
            }
        }
    }

    @Override
    public void cleanup(JobName jobName, int olderThan) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        if (olderThan <= 0) {
            throw new MaterialstoreException(
                    String.format("numberOfArtifactsToRetain=%d must be > 0",
                    olderThan));
        }
        List<JobTimestamp> diffs = store.findDifferentiatingJobTimestamps(jobName);
        if (diffs.size() > 0) {
            diffs.sort(Collections.reverseOrder());
            if (diffs.size() < olderThan) {
                this.doCleanup(jobName, diffs.get(diffs.size() - 1));
            } else {
                this.doCleanup(jobName, diffs.get(olderThan - 1));
            }
        } else {
            this.doCleanup(jobName, store.findLatestJobTimestamp(jobName));
        }
    }

    private void doCleanup(JobName jobName, JobTimestamp olderThan) throws MaterialstoreException {
        // identify which JobTimestamp directories to preserve
        Set<JobTimestamp> marked = mark(jobName, olderThan);
        // delete unnecessary JobTimestamps
        for (JobTimestamp jt : store.findAllJobTimestamps(jobName)) {
            if (!marked.contains(jt)) {
                store.deleteJobTimestamp(jobName, jt);
            }
        }
        // delete older reports other than the latest
        this.deleteReportsOlderThan(jobName, olderThan);
    }

    /**
     * check all JobTimestamp directories and mark if each should be
     * preserved. A "differentiating JobTimestamp" links to other
     * job timestamps. The referred JobTimestamps is included as marked.
     */
    private Set<JobTimestamp> mark(JobName jobName, JobTimestamp olderThan)
            throws MaterialstoreException {
        Set<JobTimestamp> marked = new HashSet<>();
        List<JobTimestamp> all = store.findAllJobTimestamps(jobName);
        all.sort(Collections.reverseOrder());
        for (JobTimestamp jt : all) {
            if (jt.compareTo(olderThan) >= 0) {
                // retain the JobTimestamp newer than the "olderThan" value
                marked.add(jt);
                if (store.hasDifferentiatingIndexEntry(jobName, jt)) {
                    // if the jt is a differentiating one, add 2 more JobTimestamps
                    // which are referred by the jt
                    List<JobTimestamp> referred = store.findJobTimestampsReferredBy(jobName, jt);
                    marked.addAll(referred);
                }
            }
        }
        return marked;
    }

    @Override
    public int deleteJobTimestampsOlderThan(JobName jobName,
                                            JobTimestamp olderThan)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(olderThan);

        // identify the JobTimestamp directories to be deleted
        List<JobTimestamp> toBeDeleted = store.findAllJobTimestampsPriorTo(jobName, olderThan);
        // now delete files/directories
        int countDeletedJT = 0;
        for (JobTimestamp jt : toBeDeleted) {
            store.deleteJobTimestamp(jobName, jt);
            countDeletedJT += 1;
        }
        return countDeletedJT;
    }

    @Override
    public int deleteReportsOlderThan(JobName jobName, JobTimestamp olderThan)
            throws MaterialstoreException {
        // identify "store/<JobName>-<JobTimestamp>.html" files to be deleted
        List<Path> reports = findAllReportsOf(jobName);
        int count = 0;
        for (Path report : reports) {
            String baseFileName =
                    store.resolveReportFileName(jobName, olderThan);
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
    public List<Path> findAllReportsOf(JobName jobName) throws MaterialstoreException {
        try (Stream<Path> stream = Files.list(store.getRoot())) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().startsWith(jobName.toString()))
                    .filter(p -> p.getFileName().toString().endsWith(".html"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

}
