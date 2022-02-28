package com.kazurayam.materialstore.map

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.metadata.Metadata

interface MappingListener {

    void onMapped(byte[] data, FileType fileType, Metadata metadata)

}
