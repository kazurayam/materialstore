package com.kazurayam.materialstore.reduce


import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import groovy.json.JsonOutput
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
                                 BiFunction<MaterialList, MaterialList, MProductGroup> func = {
                                     MaterialList previous, MaterialList current ->
                                     return MProductGroup.builder(previous, current).build()
                                 }) {
        Objects.requireNonNull(store)
        Objects.requireNonNull(currentMaterialList)

        // infer the previous MaterialList to compare the MaterialList of the current JobTimestamp against
        MaterialList previousMaterialList =
                store.queryMaterialListWithSimilarContentPriorTo(currentMaterialList)
        assert previousMaterialList.size() > 0

        //logger.info("[chronos] previousMaterialList=${JsonOutput.prettyPrint(previousMaterialList.toString())}")

        logger.info("[chronos] jobName=${currentMaterialList.getJobName()}, store=${store}")
        logger.info("[chronos] previousMaterialList.getJobTimestamp()=${previousMaterialList.getJobTimestamp()}")
        logger.info("[chronos] previousMaterialList.size()=${previousMaterialList.size()}")
        logger.info("[chronos] currentMaterialList.getJobTimestamp()=${currentMaterialList.getJobTimestamp()}")
        logger.info("[chronos] currentMaterialList.size()=${currentMaterialList.size()}")

        // zip 2 MaterialLists to form a single MProductGroup
        MProductGroup prepared = func.apply(previousMaterialList, currentMaterialList)
        assert prepared.size() > 0

        logger.info("[chronos] prepared.size()=${prepared.size()}")
        if (prepared.size() != currentMaterialList.size()) {
            logger.warn("[chronos] prepared.size() is not equal to currentMaterialList.size()")
            logger.warn(JsonOutput.prettyPrint(prepared.toString()))
        }

        return prepared
    }

    /**
     *
     */
    static MProductGroup twins(Store store,
                               MaterialList leftMaterialList,
                               MaterialList rightMaterialList,
                               BiFunction<MaterialList, MaterialList, MProductGroup> func = {
                                   MaterialList left, MaterialList right ->
                                       MProductGroup.builder(left, right)
                                               .ignoreKeys("profile", "URL.host", "URL.port")
                                               .sort("step")
                                               .build()
                               }) {
        Objects.requireNonNull(store)
        Objects.requireNonNull(leftMaterialList)
        Objects.requireNonNull(rightMaterialList)
        logger.info("[twins] store=${store}")
        logger.info("[twins] leftMaterialList=${leftMaterialList}")
        logger.info("[twins] rightMaterialList=${rightMaterialList}")
        assert leftMaterialList.size() > 0
        assert rightMaterialList.size() > 0

        // zip 2 Materials to form a single Artifact
        MProductGroup prepared = func.apply(leftMaterialList, rightMaterialList)
        assert prepared.size() > 0

        return prepared
    }
}
