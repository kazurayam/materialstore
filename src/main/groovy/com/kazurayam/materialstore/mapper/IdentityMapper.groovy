package com.kazurayam.materialstore.mapper

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.Metadata

class IdentityMapper implements Mapper {

    private Store store
    private MappingListener listener

    IdentityMapper() {}

    @Override
    void setStore(Store store) {
        this.store = store
    }

    @Override
    void setMappingListener(MappingListener listener) {
        this.listener = listener
    }


    @Override
    void map(Material material) throws IOException {
        Objects.requireNonNull(material)
        assert store != null
        assert listener != null
        //
        JobName jobName = material.getJobName()
        JobTimestamp jobTimestamp = material.getJobTimestamp()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        //
        listener.onMapped(jobber.read(material), material.getFileType(), material.getMetadata())
    }
}
