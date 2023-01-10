package com.kazurayam.materialstore.base.reduce.zipper;

import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.core.metadata.IgnoreMetadataKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * contains the "zipMaterials" method that takes 2 MaterialList objects (left, right)
 * together with some parameters, create a List of MaterialProduct objects.
 * 2 MaterialLists are converted into a single List of MaterialProduct.
 * It is the reason why this method and this class is named "zipper".
 */
public final class Zipper {

    private static Logger logger = LoggerFactory.getLogger(Zipper.class);

    private final IgnoreMetadataKeys ignoreMetadataKeys;
    private final IdentifyMetadataValues identifyMetadataValues;

    public Zipper(final IgnoreMetadataKeys ignoreMetadataKeys,
                  final IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(ignoreMetadataKeys);
        Objects.requireNonNull(identifyMetadataValues);
        this.ignoreMetadataKeys = ignoreMetadataKeys;
        this.identifyMetadataValues = identifyMetadataValues;
    }

    /*
     *
     */
    public List<MaterialProduct> zipMaterials(
            final MaterialList leftList,
            final MaterialList rightList,
            final JobTimestamp resultTimestamp) {
        Objects.requireNonNull(leftList);
        Objects.requireNonNull(rightList);
        Objects.requireNonNull(resultTimestamp);
        String methodName = "#zipMaterials ";
        // the result
        final List<MaterialProduct> mProductList = new ArrayList<>();

        for (Material right : rightList) {
            final IFileType rightFileType = right.getIndexEntry().getFileType();
            Metadata rightMetadata = right.getIndexEntry().getMetadata();
            final QueryOnMetadata rightPattern =
                    QueryOnMetadata.builder(rightMetadata, ignoreMetadataKeys).build();
            //
            logger.debug(methodName + "--------");
            logger.debug(methodName + "right " + right.getShortID() + " "
                    + rightFileType.getExtension() + " pattern: " + rightPattern);
            int foundLeftCount = 0;

            for (Material left : leftList) {
                final IFileType leftFileType = left.getIndexEntry().getFileType();
                final Metadata leftMetadata = left.getIndexEntry().getMetadata();
                if (leftFileType.equals(rightFileType) && (rightPattern.matches(leftMetadata)
                        || identifyMetadataValues.matches(leftMetadata))) {
                    MaterialProduct mp =
                            new MaterialProduct.Builder(left, right,
                                    right.getJobName(), resultTimestamp)
                                    .setQueryOnMetadata(rightPattern)
                                    .build();
                    mProductList.add(mp);
                    logger.debug(methodName + "left Y " + left.getShortID() + " "
                            + leftFileType.getExtension() + " " + leftMetadata);
                    foundLeftCount += 1;
                } else {
                    logger.debug(methodName + "left N " + left.getShortID() + " "
                            + leftFileType.getExtension() + " " + leftMetadata);
                }
            }
            if (foundLeftCount == 0) {
                MaterialProduct mp =
                        new MaterialProduct.Builder(Material.newEmptyMaterial(), right,
                                right.getJobName(), resultTimestamp)
                                .setQueryOnMetadata(rightPattern)
                                .build();
                mProductList.add(mp);
            }

            if (foundLeftCount == 0 || foundLeftCount >= 2) {
                logger.info(methodName + "foundLeftCount=" + foundLeftCount + " is unusual");
            }
        }

        //
        for (Material left : leftList) {
            final IFileType leftFileType = left.getIndexEntry().getFileType();
            Metadata leftMetadata = left.getIndexEntry().getMetadata();
            final QueryOnMetadata leftPattern =
                    QueryOnMetadata.builder(leftMetadata, ignoreMetadataKeys).build();
            logger.debug(methodName + "--------");
            logger.debug(methodName + "left " + left.getShortID() + " "
                    + leftFileType.toString() + " pattern: " + leftPattern);
            int foundRightCount = 0;
            for (Material right : rightList) {
                final IFileType rightFileType = right.getIndexEntry().getFileType();
                final Metadata rightMetadata = right.getIndexEntry().getMetadata();
                if (rightFileType.equals(leftFileType) && (leftPattern.matches(rightMetadata)
                                || identifyMetadataValues.matches(rightMetadata))) {
                    // this must have been found matched already; no need to create a MProduct
                    logger.debug(methodName + "right Y " + right.getShortID() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                    foundRightCount += 1;
                } else {
                    logger.debug(methodName + "right N " + right.getShortID() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                }
            }
            if (foundRightCount == 0) {
                MaterialProduct mProduct =
                        new MaterialProduct.Builder(left, Material.newEmptyMaterial(),
                                left.getJobName(), resultTimestamp)
                                .setQueryOnMetadata(leftPattern)
                                .build();
                mProductList.add(mProduct);
            }

            //logger.debug(methodName + "foundRightCount=" + foundRightCount);
            if (foundRightCount == 0 || foundRightCount >= 2) {
                logger.info(methodName + "foundRightCount=" + foundRightCount + " is unusual");
            }
        }
        Collections.sort(mProductList);
        return mProductList;
    }
}
