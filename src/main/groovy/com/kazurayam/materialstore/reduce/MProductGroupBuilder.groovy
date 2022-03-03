package com.kazurayam.materialstore.reduce


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.BiFunction

class MProductGroupBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroupBuilder.class)

    private MProductGroupBuilder() {}

    /**
     *
     */
    static MProductGroup chronos(Store store,
                                 MaterialList currentMaterialList,
                                 BiFunction<MaterialList, MaterialList, MProductGroup> mProductGroupBuilder = {
                                     MaterialList previous, MaterialList current ->
                                     return MProductGroup.builder(previous, current).build()
                                 }) {
        Objects.requireNonNull(store)
        Objects.requireNonNull(currentMaterialList)

        JobName jobName = currentMaterialList.getJobName()
        JobTimestamp currentTimestamp = currentMaterialList.getJobTimestamp()

        // infer the previous JobTimestamp to compare the current JobTimestamp against
        JobTimestamp previousTimestamp =
                store.queryJobTimestampWithSimilarContentPriorTo(jobName, currentTimestamp)

        logger.info("[reduceChronos] jobName=${jobName}, store=${store}")
        logger.info("[reduceChronos] previousTimestamp=${previousTimestamp}")
        logger.info("[reduceChronos] currentTimestamp=${currentTimestamp}")

        // Look up the materials stored in the previous time of run
        MaterialList previousML = store.select(jobName, previousTimestamp, QueryOnMetadata.ANY)
        assert previousML.size() > 0

        // zip 2 MaterialLists to form a single MProductGroup
        MProductGroup prepared = mProductGroupBuilder.apply(previousML, currentMaterialList)
        assert prepared.size() > 0

        return prepared
    }

    /**
     *
     */
    static MProductGroup twins(Store store,
                               MaterialList leftMaterialList,
                               MaterialList rightMaterialList,
                               BiFunction<MaterialList, MaterialList, MProductGroup> mProductGroupBuilder = {
                                   MaterialList left, MaterialList right ->
                                       MProductGroup.builder(left, right)
                                               .ignoreKeys("profile", "URL.host", "URL.port")
                                               .sort("step")
                                               .build()
                               }) {
        Objects.requireNonNull(store)
        Objects.requireNonNull(leftMaterialList)
        Objects.requireNonNull(rightMaterialList)
        logger.info("[reduceTwins] store=${store}")
        logger.info("[reduceTwins] leftMaterialList=${leftMaterialList}")
        logger.info("[reduceTwins] rightMaterialList=${rightMaterialList}")
        assert leftMaterialList.size() > 0
        assert rightMaterialList.size() > 0

        // zip 2 Materials to form a single Artifact
        MProductGroup prepared = mProductGroupBuilder.apply(leftMaterialList, rightMaterialList)
        assert prepared.size() > 0

        return prepared
    }
}
