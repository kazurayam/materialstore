package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.FileTypeDiffability;
import com.kazurayam.materialstore.core.filesystem.Jobber;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialLocator;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import java.awt.image.BufferedImage;
import java.util.Objects;

public final class ImageDifferToPNG implements Differ {

    private final Store store;

    public ImageDifferToPNG(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());

        Material left = complementMaterialAsImage(store, mProduct, mProduct.getLeft());
        BufferedImage leftImage = readImage(left.toFile(store));

        Material right = complementMaterialAsImage(store, mProduct, mProduct.getRight());
        BufferedImage rightImage = readImage(right.toFile(store));

        // make a diff image using AShot
        ImageDiffer imgDiff = new ImageDiffer();
        ImageDiff imageDiff = imgDiff.makeDiff(leftImage, rightImage);
        Double diffRatio = calculateDiffRatioPercent(imageDiff);
        //
        Metadata diffMetadata =
                Metadata.builder()
                        .put("category", "diff")
                        .put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio))
                        .put("left", new MaterialLocator(left).toString())
                        .put("right", new MaterialLocator(right).toString())
                        .build();

        byte[] diffData = toByteArray(imageDiff.getDiffImage(), FileType.PNG);

        // write the image diff
        Material diffMaterial =
                new Jobber(store, mProduct.getJobName(), mProduct.getReducedTimestamp())
                        .write(diffData, FileType.PNG, diffMetadata,
                                Jobber.DuplicationHandling.CONTINUE);
        //
        MaterialProduct result = new MaterialProduct.Builder(mProduct)
                .setLeft(left)
                .setRight(right)
                .build();
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);

        return result;
    }

    /*
     * if the given Material object is OK as an image, just return it.
     * if the given Material object is empty one, swap it to the "No Material is found" image.
     */
    private Material complementMaterialAsImage(Store store,
                                               MaterialProduct mProduct,
                                               Material material) throws MaterialstoreException {
        if (material.getDiffability().equals(FileTypeDiffability.AS_IMAGE)) {
            return material;
        } else {
            return makeNoMaterialFoundMaterial(store, mProduct,
                    FileType.PNG, Material.loadNoMaterialFoundPng());
        }
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
