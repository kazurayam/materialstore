package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;

public abstract class MappingListener {

    public abstract void onMapped(byte[] data, IFileType fileType, Metadata metadata) throws MaterialstoreException;

}
