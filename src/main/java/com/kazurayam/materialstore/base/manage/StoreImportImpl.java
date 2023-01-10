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

public class StoreImportImpl extends StoreImport {

    private final Store remote;
    private final Store local;

    public StoreImportImpl(Store remote, Store local) {
        Objects.requireNonNull(remote);
        Objects.requireNonNull(local);
        this.remote = remote;
        this.local = local;
    }

    @Override
    public void importReports(JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        JobTimestamp jt = remote.findLatestJobTimestamp(jobName);
        if (jt != JobTimestamp.NULL_OBJECT) {
            this.importReports(jobName, jt);
        }
    }

    @Override
    public void importReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(newerThanOrEqualTo);
        importJobTimestamps(jobName, newerThanOrEqualTo);
        importReportFiles(jobName, newerThanOrEqualTo);
    }

    private void importJobTimestamps(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked =
                remote.markNewerThanOrEqualTo(jobName, newerThanOrEqualTo);
        for (JobTimestamp jt : marked) {
            Path source = remote.getPathOf(jobName, jt);
            Path target =
                    local.getRoot().resolve(jobName.toString()).resolve(jt.toString());
            try {
                if (!Files.exists(target)) {
                    Files.createDirectories(target);
                }
                // If a file is already existing in the local store,
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

    private void importReportFiles(JobName jobName, JobTimestamp newerThanOrEqualTo)
            throws MaterialstoreException {
        Set<JobTimestamp> marked =
                remote.markNewerThanOrEqualTo(jobName, newerThanOrEqualTo);
        for (JobTimestamp jt : marked) {
            Path source = remote.getRoot().resolve(
                    remote.resolveReportFileName(jobName, jt));
            Path target = local.getRoot().resolve(
                    local.resolveReportFileName(jobName, jt));
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
