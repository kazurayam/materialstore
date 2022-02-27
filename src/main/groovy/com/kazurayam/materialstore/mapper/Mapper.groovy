package com.kazurayam.materialstore.mapper

import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store

interface Mapper {

    void setStore(Store store)

    byte[] map(Material material)

}