package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BiFunction;

public class MProductGroupBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroupBuilder.class);

    private MProductGroupBuilder() {}

    /**
     *
     */
    public static MProductGroup chronos(Store store,
                                        MaterialList currentMaterialList,
                                        BiFunction<MaterialList, MaterialList, MProductGroup> func)
            throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(currentMaterialList);

        // infer the previous MaterialList to compare the MaterialList of the current JobTimestamp against
        MaterialList previousMaterialList = store.reflect(currentMaterialList);

        assert previousMaterialList.size() > 0;

        logger.info("[chronos] jobName=" + String.valueOf(currentMaterialList.getJobName()) + ", store=" + String.valueOf(store));
        logger.info("[chronos] previousMaterialList.getJobTimestamp()=" + String.valueOf(previousMaterialList.getJobTimestamp()));
        logger.info("[chronos] previousMaterialList.size()=" + String.valueOf(previousMaterialList.size()));
        logger.info("[chronos] currentMaterialList.getJobTimestamp()=" + String.valueOf(currentMaterialList.getJobTimestamp()));
        logger.info("[chronos] currentMaterialList.size()=" + String.valueOf(currentMaterialList.size()));

        // zip 2 MaterialLists to form a single MProductGroup
        MProductGroup prepared = func.apply(previousMaterialList, currentMaterialList);
        assert prepared.size() > 0;

        logger.info("[chronos] prepared.size()=" + String.valueOf(prepared.size()));
        if (prepared.size() != currentMaterialList.size()) {
            logger.warn("[chronos] prepared.size() is not equal to currentMaterialList.size()");
            logger.warn(JsonOutput.prettyPrint(prepared.toString()));
        }

        return prepared;
    }


    /**
     *
     */
    public static MProductGroup twins(Store store, MaterialList leftMaterialList, MaterialList rightMaterialList, BiFunction<MaterialList, MaterialList, MProductGroup> func) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(leftMaterialList);
        Objects.requireNonNull(rightMaterialList);
        logger.info("[twins] store=" + String.valueOf(store));
        logger.info("[twins] leftMaterialList=" + String.valueOf(leftMaterialList));
        logger.info("[twins] rightMaterialList=" + String.valueOf(rightMaterialList));
        assert leftMaterialList.size() > 0;
        assert rightMaterialList.size() > 0;

        // zip 2 Materials to form a single Artifact
        MProductGroup prepared = func.apply(leftMaterialList, rightMaterialList);
        assert prepared.size() > 0;

        return prepared;
    }

}
