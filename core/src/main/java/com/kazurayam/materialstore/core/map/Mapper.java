package com.kazurayam.materialstore.core.map;

import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;

public interface Mapper {

    void setStore(Store store);

    void setMappingListener(MappingListener listener);

    /**
     * MappingListener#onMapped() will be called back once or more times
     */
    void map(Material material) throws MaterialstoreException;
}
