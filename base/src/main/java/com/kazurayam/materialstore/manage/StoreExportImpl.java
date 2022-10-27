package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;

import java.util.Objects;

public class StoreExportImpl extends StoreExport {

    private final Store local;
    private final Store remote;

    public StoreExportImpl(Store local, Store remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public void exportReports(JobName jobName) throws MaterialstoreException {
        JobTimestamp jt = local.findLatestJobTimestamp(jobName);
        if (jt != JobTimestamp.NULL_OBJECT) {
            this.exportReports(jobName, jt);
        }
    }
    @Override
    public void exportReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
    }
}
