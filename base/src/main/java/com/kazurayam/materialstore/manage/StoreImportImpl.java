package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;

import java.util.Objects;

public class StoreImportImpl extends StoreImport {

    private final Store remote;
    private final Store local;

    public StoreImportImpl(Store remote, Store local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public void importReports(JobName jobName) throws MaterialstoreException {
        JobTimestamp jt = remote.findLatestJobTimestamp(jobName);
        if (jt != JobTimestamp.NULL_OBJECT) {
            this.importReports(jobName, jt);
        }
    }

    @Override
    public void importReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
    }
}
