package com.kazurayam.materialstore.mapper

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.metadata.Metadata

interface MappingListener {

    void onMapped(byte[] data, FileType fileType, Metadata metadata)

}
