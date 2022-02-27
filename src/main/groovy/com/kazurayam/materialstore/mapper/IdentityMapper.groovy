package com.kazurayam.materialstore.mapper

import com.kazurayam.materialstore.filesystem.ID
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store

class IdentityMapper implements Mapper {

    private Store store

    IdentityMapper() {}

    void setStore(Store store) {
        this.store = store
    }

    @Override
    byte[] map(Material material) {
        Objects.requireNonNull(material)
        assert store != null
        JobName jobName = material.getJobName()
        JobTimestamp jobTimestamp = material.getJobTimestamp()
        Jobber jobber = store.getJobber(jobName, jobTimestamp)
        return jobber.read(material)
    }
}
