package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Store;

public class StoreDirectory {
    private final Store store;
    private final JobName jobName;
    private final JobTimestamp jobTimestamp;
    private StoreDirectory(Store store, JobName jobName, JobTimestamp jobTimestamp) {
        this.store = store;
        this.jobName = jobName;
        this.jobTimestamp = jobTimestamp;
    }
}
