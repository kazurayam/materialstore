package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.filesystem.IFileType;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;

public abstract class MappingListener {

    public abstract void onMapped(byte[] data, IFileType fileType, Metadata metadata) throws MaterialstoreException;

}
