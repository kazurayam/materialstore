package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.StoreImpl;

public abstract class MappingListener {

    public abstract void onMapped(byte[] data, FileType fileType, Metadata metadata) throws MaterialstoreException;

}
