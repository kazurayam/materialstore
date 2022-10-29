package com.kazurayam.materialstore.core.map;

import com.kazurayam.materialstore.core.filesystem.IFileType;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;

public abstract class MappingListener {

    public abstract void onMapped(byte[] data, IFileType fileType, Metadata metadata) throws MaterialstoreException;

}
