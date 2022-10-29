package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;

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
