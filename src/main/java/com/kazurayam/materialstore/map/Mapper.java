package com.kazurayam.materialstore.map;

import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;

public interface Mapper {

    void setStore(Store store);

    void setMappingListener(MappingListener listener);

    /*
     * MappingListener#onMapped() will be called back once or more times
     */
    void map(Material material) throws MaterialstoreException;
}
