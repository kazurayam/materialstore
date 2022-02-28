package com.kazurayam.materialstore.mapper

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.Metadata

class MappedResultSerializer implements MappingListener {

    private Store store
    private JobName jobName
    private JobTimestamp jobTimestamp

    MappedResultSerializer(Store store, JobName jobName, JobTimestamp jobTimestamp) {
        this.store = store
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
    }

    @Override
    void onMapped(byte[] data, FileType fileType, Metadata metadata) {
        store.write(jobName, jobTimestamp, fileType, metadata, data)
    }
}

