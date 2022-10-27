package com.kazurayam.materialstore.manage;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;

/**
 * StoreImport class helps import files from a remote store into a local store
 */
public abstract class StoreImport {

    public abstract void importReports(JobName jobName) throws MaterialstoreException;
    public abstract void importReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException;

    public static StoreImport newInstance(Store fromRemote, Store toLocal) {
        return new StoreImportImpl(fromRemote, toLocal);
    }
}
