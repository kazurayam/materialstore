package com.kazurayam.materialstore.base.manage;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;

public abstract class StoreCleaner {

    public static StoreCleaner newInstance(Store store) {
        return new StoreCleanerImpl(store);
    }

    /*
     * Clean up the store directory for the given JobName. This will do 2 clean-up actions.
     *
     * (1) delete unnecessary JobTimestamp directories. Beware there are 2 cases.
     *
     * First, the JobTimestamp directory was created by Inspector.reduceAndSort() method.
     * the latest JobTimestamp contains an "index" file that records with "category":"diff"
     * attribute. The "diff" records contains "left":"anotherJobTimestamp/xxxxxxxxxxxxxxxx..." and
     * "right":"anotherJobTimestamp/xxxxxxxxxxxxxxxxxxx..." attributes. The "anotherJobTimestamp"
     * locates other JobTimestamp directories that should be preserved to keep the links are not broken.
     * The cleanup method will retain the latest JobTimestamp and other JobTimestamps that are
     * linked by the latest JobTimestamp.
     *
     * Second, the latest JobTimestamp was not created by Inspector.reduceAndSort() method.
     * In this case, the latest JobTimestamp contains an "index" file without "category":"diff".
     * In this case, the cleanup method retain only the latest JobTimestamp. It will delete
     * all other JobTimestamps older than the latest.
     *
     * (2) the cleanup method identifies a list of report HTML files that belongs to the JobName.
     * the cleanup method retains only the latest report, will delete all the rest.
     *
     * @param jobName
     * @throws IOException
     * @throws MaterialstoreException
     */
    public abstract void cleanup(JobName jobName)
            throws MaterialstoreException;

    public abstract void cleanup(JobName jobName, JobTimestamp olderThan)
            throws MaterialstoreException;

    public abstract void cleanup(JobName jobName, int olderThan)
            throws MaterialstoreException;


    public abstract int deleteJobTimestampsOlderThan(
            JobName jobName, JobTimestamp olderThan) throws MaterialstoreException;

    public abstract int deleteReportsOlderThan(
            JobName jobName, JobTimestamp olderThan) throws MaterialstoreException;

}
