package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.FileTypeDiffability;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialLocator;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.ashot.comparison.DiffMarkupPolicy;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.ImageMarkupPolicy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;

public final class ImageDiffStuffer implements Differ {

    private static final Logger logger = LoggerFactory.getLogger(ImageDiffStuffer.class);

    private final Store store;

    public ImageDiffStuffer(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public MaterialProduct generateDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());
        Material left = mProduct.getLeft();
        Material right = mProduct.getRight();
        Material diffMaterial = null;
        Double diffRatio = 0.0d;
        if (left.getDiffability().equals(FileTypeDiffability.AS_IMAGE) &&
                right.getDiffability().equals(FileTypeDiffability.AS_IMAGE)) {
            // Both of the left and right Materials are diff-able as image
            BufferedImage leftImage = readImage(left.toPath());
            BufferedImage rightImage = readImage(right.toPath());
            // make a diff image using AShot
            DiffMarkupPolicy dmp = new ImageMarkupPolicy()
                            .withDiffColor(mProduct.getWithDiffColor().getColor());
            ImageDiff imageDiff = new ImageDiffer()
                    .withDiffMarkupPolicy(dmp)
                    .makeDiff(leftImage, rightImage);
            diffRatio = calculateDiffRatioPercent(imageDiff);
            // write the diff image into the store
            Metadata diffMetadata = Metadata.builder()
                    .put("category", "diff")
                    .put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio))
                    .put("left", new MaterialLocator(left).toString())
                    .put("right", new MaterialLocator(right).toString())
                    .build();
            diffMaterial =
                    store.write(mProduct.getJobName(), mProduct.getReducedTimestamp(),
                            right.getFileType(), diffMetadata,
                            imageDiff.getDiffImage());
        } else {
            // Either of the left or the right Material is non diff-able as image
            diffRatio = 100.0d;
            Metadata diffMetadata = Metadata.builder()
                    .put("category", "diff")
                    .put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio))
                    .put("left", new MaterialLocator(left).toString())
                    .put("right", new MaterialLocator(right).toString())
                    .build();
            diffMaterial =
                    store.write(mProduct.getJobName(), mProduct.getReducedTimestamp(),
                            FileType.NO_COUNTERPART, diffMetadata,
                            Material.loadNoCounterpartPng());
        }
        // stuff the diffMaterial into a MaterialProduct object and return it
        MaterialProduct result = new MaterialProduct.Builder(mProduct).build();
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);
        return result;
    }

    /**
     * Calculate the ratio of diff-size against the whole page size.
     * <p>
     * The result is rounded up.  E.g. 0.0001 to 0.01
     */
    private static Double calculateDiffRatioPercent(ImageDiff diff) {
        boolean hasDiff = diff.hasDiff();
        if (!hasDiff) {
            return 0.0;
        }

        int diffSize = diff.getDiffSize();
        int area = diff.getMarkedImage().getWidth() * diff.getMarkedImage().getHeight();
        Double diffRatio = diffSize * 1.0D / area * 100;
        return DifferUtil.roundUpTo2DecimalPlaces(diffRatio);
    }

}
