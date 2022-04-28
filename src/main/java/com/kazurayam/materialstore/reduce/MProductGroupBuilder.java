package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BiFunction;

public final class MProductGroupBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroupBuilder.class);

    private MProductGroupBuilder() {}

    public static MProductGroup chronos(Store store,
                                        MaterialList currentMaterialList)
        throws MaterialstoreException {

        return chronos(store, currentMaterialList, currentMaterialList.getJobTimestamp());
    }

    public static MProductGroup chronos(Store store,
                                        MaterialList currentMaterialList,
                                        BiFunction<MaterialList, MaterialList, MProductGroup> func)
            throws MaterialstoreException {
        return chronos(store, currentMaterialList, currentMaterialList.getJobTimestamp(), func);
    }
    /**
     *
     */
    public static MProductGroup chronos(Store store,
                                        MaterialList currentMaterialList,
                                        JobTimestamp priorTo)
            throws MaterialstoreException {
        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList left, MaterialList right) ->
                        MProductGroup.builder(left,right).build();
        return chronos(store, currentMaterialList, priorTo, func);
    }

    /**
     *
     */
    public static MProductGroup chronos(Store store,
                                        MaterialList currentMaterialList,
                                        JobTimestamp priorTo,
                                        BiFunction<MaterialList, MaterialList, MProductGroup> func)
            throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(currentMaterialList);
        Objects.requireNonNull(priorTo);
        Objects.requireNonNull(func);
        logger.info("[chronos] jobName=" + currentMaterialList.getJobName() + ", store=" + store);
        logger.info("[chronos] currentMaterialList.getJobTimestamp()=" + currentMaterialList.getJobTimestamp());
        logger.info("[chronos] currentMaterialList.size()=" + currentMaterialList.size());
        logger.info("[chronos] priorTo=" + priorTo);
        logger.info("[chronos] func=" + func);

        // infer the previous MaterialList to compare the MaterialList of the current JobTimestamp against
        MaterialList previousMaterialList = store.reflect(currentMaterialList, priorTo);
        logger.info("[chronos] previousMaterialList.getJobTimestamp()=" + previousMaterialList.getJobTimestamp());
        logger.info("[chronos] previousMaterialList.size()=" + previousMaterialList.size());

        if (previousMaterialList.size() == 0) {
            throw new MaterialstoreException(
                    String.format("store.reflect(currentMaterialList) returned previousMaterialList of size == 0. " +
                            "currentMaterialList = %s/%s", currentMaterialList.getJobName().toString(),
                            currentMaterialList.getJobTimestamp().toString()));
        }

        // zip 2 MaterialLists to form a single MProductGroup
        MProductGroup prepared = func.apply(previousMaterialList, currentMaterialList);
        assert prepared.size() > 0;

        logger.info("[chronos] prepared.size()=" + prepared.size());
        if (prepared.size() != currentMaterialList.size()) {
            logger.warn("[chronos] prepared.size() is not equal to currentMaterialList.size()");
            logger.warn(JsonUtil.prettyPrint(prepared.toString()));
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
        logger.info("[twins] store=" + store);
        logger.info("[twins] leftMaterialList=" + leftMaterialList);
        logger.info("[twins] rightMaterialList=" + rightMaterialList);
        assert leftMaterialList.size() > 0;
        assert rightMaterialList.size() > 0;

        // zip 2 Materials to form a single Artifact
        MProductGroup prepared = func.apply(leftMaterialList, rightMaterialList);
        assert prepared.size() > 0;

        return prepared;
    }

}
