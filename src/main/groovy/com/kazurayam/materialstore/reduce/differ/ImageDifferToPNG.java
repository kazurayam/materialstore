package com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.FileTypeDiffability;
import com.kazurayam.materialstore.filesystem.Jobber;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class ImageDifferToPNG implements Differ {
    public ImageDifferToPNG() {
    }

    public ImageDifferToPNG(Path root) {
        ensureRoot(root);
        this.root_ = root;
    }

    public void setRoot(Path root) {
        ensureRoot(root);
        this.root_ = root;
    }

    private static void ensureRoot(final Path root) {
        Objects.requireNonNull(root);
        if (!Files.exists(root)) {
            throw new IllegalArgumentException(root + " is not present");
        }

    }

    @Override
    public MaterialProduct makeMProduct(MaterialProduct mProduct) throws MaterialstoreException {
        Objects.requireNonNull(root_);
        Objects.requireNonNull(mProduct);
        Objects.requireNonNull(mProduct.getLeft());
        Objects.requireNonNull(mProduct.getRight());
        //
        final Material left = mProduct.getLeft();
        if (!left.getDiffability().equals(FileTypeDiffability.AS_IMAGE)) {
            throw new IllegalArgumentException("the left material is not an image: " + left);
        }

        File leftFile = root_.resolve(left.getRelativePath()).toFile();
        BufferedImage leftImage = readImage(leftFile);
        assert leftImage != null;
        //
        final Material right = mProduct.getRight();
        if (!right.getDiffability().equals(FileTypeDiffability.AS_IMAGE)) {
            throw new IllegalArgumentException("the right material is not an image: " + right);
        }

        File rightFile = root_.resolve(right.getRelativePath()).toFile();
        BufferedImage rightImage = readImage(rightFile);
        assert rightImage != null;

        // make a diff image using AShot
        ImageDiffer imgDiff = new ImageDiffer();
        ImageDiff imageDiff = imgDiff.makeDiff(leftImage, rightImage);
        Double diffRatio = calculateDiffRatioPercent(imageDiff);
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("category", "diff");
        map.put("ratio", DifferUtil.formatDiffRatioAsString(diffRatio));
        map.put("left", left.getIndexEntry().getID().toString());
        map.put("right", right.getIndexEntry().getID().toString());
        Metadata diffMetadata = Metadata.builder(map).build();
        byte[] diffData = toByteArray(imageDiff.getDiffImage(), FileType.PNG);
        // write the image diff into disk
        Jobber jobber = new Jobber(root_, right.getJobName(), mProduct.getReducedTimestamp());
        Material diffMaterial = jobber.write(diffData, FileType.PNG, diffMetadata, Jobber.DuplicationHandling.CONTINUE);

        //
        MaterialProduct result = new MaterialProduct(mProduct);
        result.setDiff(diffMaterial);
        result.setDiffRatio(diffRatio);
        return result;
    }

    private static BufferedImage readImage(final File imageFile) {
        if (!imageFile.exists()) {
            throw new IllegalArgumentException(imageFile + " is not found");
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            assert bufferedImage != null;
            return bufferedImage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static byte[] toByteArray(BufferedImage input, FileType fileType)
            throws MaterialstoreException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(input, fileType.getExtension(), baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new MaterialstoreException(e);
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

    private Path root_;
}
