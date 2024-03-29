package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobNameNotFoundException;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;

/**
 * StoreImport class helps import files from a remote store into a local store
 */
public abstract class StoreImport {

    public abstract void importReports(JobName jobName) throws MaterialstoreException, JobNameNotFoundException;
    public abstract void importReports(JobName jobName, JobTimestamp newerThanOrEqualTo) throws MaterialstoreException, JobNameNotFoundException;

    public static StoreImport newInstance(Store fromRemote, Store toLocal) {
        return new StoreImportImpl(fromRemote, toLocal);
    }
}
