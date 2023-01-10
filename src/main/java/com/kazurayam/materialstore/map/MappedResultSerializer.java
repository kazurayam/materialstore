package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.StoreImpl;

public final class MappedResultSerializer extends MappingListener {

    public static MappedResultSerializer NULL_OBJECT =
            new MappedResultSerializer(StoreImpl.NULL_OBJECT, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT);

    private final Store store;
    private final JobName jobName;
    private final JobTimestamp jobTimestamp;

    public MappedResultSerializer(Store store, JobName jobName, JobTimestamp jobTimestamp) {
        this.store = store;
        this.jobName = jobName;
        this.jobTimestamp = jobTimestamp;
    }

    @Override
    public void onMapped(byte[] data, IFileType fileType, Metadata metadata) throws MaterialstoreException {
        store.write(jobName, jobTimestamp, fileType, metadata, data);
    }


}
