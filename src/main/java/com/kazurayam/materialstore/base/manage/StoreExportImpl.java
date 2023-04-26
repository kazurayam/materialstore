package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.util.CopyDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class StoreExportImpl extends StoreExport {

    private final Store local;
    private final Store remote;

    public StoreExportImpl(Store local, Store remote) {
        Objects.requireNonNull(local);
        Objects.requireNonNull(remote);
        this.local = local;
        this.remote = remote;
    }


    /**
     *
     */
    @Override
    public void exportReports(JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
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
        Objects.requireNonNull(newerThanOrEqualTo);
        exportJobTimestamps(jobName, newerThanOrEqualTo);
        exportReportFiles(jobName, newerThanOrEqualTo);
    }


    private void exportJobTimestamps(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked =
                local.markNewerThanOrEqualTo(jobName, newerThanOrEqualTo);
        for (JobTimestamp jt : marked) {
            Path source = local.getPathOf(jobName, jt);
            Path target =
                    remote.getRoot().resolve(jobName.toString()).resolve(jt.toString());
            try {
                if (!Files.exists(target)) {
                    Files.createDirectories(target);
                }
                // If a file is already existing in the remote store,
                // we will skip copying it.
                // It is to shorten the processing time.
                Files.walkFileTree(source,
                        new CopyDir(source, target,
                                CopyDir.Option.SKIP_IF_EXISTING));
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
        }
    }


    private void exportReportFiles(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked =
                local.markNewerThanOrEqualTo(jobName, newerThanOrEqualTo);
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

}
