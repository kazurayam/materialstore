package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;

/**
 * StoreExport class helps export files from a local store into a remote store.
 */
public abstract class StoreExport {

    public abstract void exportReports(JobName jobName) throws MaterialstoreException;
    public abstract void exportReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException;

    public static StoreExport newInstance(Store fromLocal, Store toRemote) {
        return new StoreExportImpl(fromLocal, toRemote);
    }
}
