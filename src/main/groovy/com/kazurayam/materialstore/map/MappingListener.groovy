package com.kazurayam.materialstore.map

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.StoreImpl

abstract class MappingListener {

    public static MappingListener NULL_OBJECT =
            new MappedResultSerializer(StoreImpl.NULL_OBJECT, JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT);

    abstract void onMapped(byte[] data, FileType fileType, Metadata metadata)

}
