package com.kazurayam.materialstore

import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.diffartifact.SortKeys
import com.kazurayam.materialstore.differ.DifferDriver
import com.kazurayam.materialstore.differ.DifferDriverImpl
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.MetadataPattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaterialstoreFacade {

    private final Store store

    public MaterialstoreFacade(Store store) {
        this.store = store
    }

    DiffArtifactGroup makeDiff(DiffArtifactGroup preparedGroup) {
        DifferDriver differDriver = new DifferDriverImpl.Builder(store.getRoot()).build()
        return makeDiff(preparedGroup, differDriver)
    }

    static DiffArtifactGroup makeDiff(DiffArtifactGroup preparedGroup, DifferDriver differDriver) {
        Objects.requireNonNull(preparedGroup)
        Objects.requireNonNull(differDriver)
        DiffArtifactGroup stuffed = differDriver.differentiate(preparedGroup)
        stuffed.sort()
        return stuffed
    }
}
