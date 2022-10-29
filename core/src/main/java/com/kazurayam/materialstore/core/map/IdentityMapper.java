package com.kazurayam.materialstore.core.map;

import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Jobber;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;

import java.util.Objects;

public final class IdentityMapper implements Mapper {
    public IdentityMapper() {
    }

    @Override
    public void setStore(Store store) {
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        this.listener = listener;
    }

    @Override
    public void map(Material material) throws MaterialstoreException {
        Objects.requireNonNull(material);
        assert store != null;
        assert listener != null;
        //
        JobName jobName = material.getJobName();
        JobTimestamp jobTimestamp = material.getJobTimestamp();
        Jobber jobber = store.getJobber(jobName, jobTimestamp);
        //
        listener.onMapped(jobber.read(material), material.getFileType(), material.getMetadata());
    }

    private Store store;
    private MappingListener listener;
}
