package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.util.CopyDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class StoreExportImpl extends StoreExport {

    private final Store local;
    private final Store remote;

    public StoreExportImpl(Store local, Store remote) {
        this.local = local;
        this.remote = remote;
    }


    /**
     *
     */
    @Override
    public void exportReports(JobName jobName) throws MaterialstoreException {
        JobTimestamp jt = local.findLatestJobTimestamp(jobName);
        if (jt != JobTimestamp.NULL_OBJECT) {
            this.exportReports(jobName, jt);
        }
    }

    /**
     *
     */
    @Override
    public void exportReports(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        exportJobTimestamps(jobName, newerThanOrEqualTo);
        exportReportFiles(jobName, newerThanOrEqualTo);
    }


    private void exportJobTimestamps(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked = markJobTimestampsToExport(jobName, newerThanOrEqualTo);
        for (JobTimestamp jt : marked) {
            Path source = local.getPathOf(jobName, jt);
            Path target = remote.getRoot()
                            .resolve(jobName.toString())
                            .resolve(jt.toString());
            try {
                if (!Files.exists(target)) {
                    Files.createDirectories(target);
                }
                Files.walkFileTree(source,
                        new CopyDir(source, target,
                                CopyDir.Option.SKIP_IF_EXISTING));
                // If a file is already existing in the remote store,
                // we will skip copying it.
                // It is to shorten the processing time.
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
        }
    }


    private void exportReportFiles(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked = markJobTimestampsToExport(jobName, newerThanOrEqualTo);
        for (JobTimestamp jt : marked) {
            Path source = local.getRoot().resolve(
                    local.resolveReportFileName(jobName, jt));
            Path target = remote.getRoot().resolve(
                    remote.resolveReportFileName(jobName, jt));
            if (Files.exists(source)) {
                try {
                    Files.walkFileTree(source,
                            new CopyDir(source, target,
                                    CopyDir.Option.SKIP_IF_EXISTING));
                } catch (IOException e) {
                    throw new MaterialstoreException(e);
                }
            }
        }
    }


    private Set<JobTimestamp> markJobTimestampsToExport(JobName jobName,
                                                        JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked = new HashSet<>();
        List<JobTimestamp> all = local.findAllJobTimestamps(jobName);
        if (all.size() > 0) {
            all.sort(Comparator.reverseOrder());
            for (JobTimestamp jt : all) {
                if (jt.compareTo(newerThanOrEqualTo) >= 0) {
                    marked.add(jt);
                    List<JobTimestamp> referred = local.findJobTimestampsReferredBy(jobName, jt);
                    if (referred.size() > 0) {
                        marked.addAll(referred);
                    }
                }
            }
        }
        return marked;
    }

}
