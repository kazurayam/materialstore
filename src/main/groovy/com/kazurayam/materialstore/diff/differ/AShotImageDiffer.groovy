package com.kazurayam.materialstore.diff.differ

import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import ru.yandex.qatools.ashot.comparison.ImageDiff
import ru.yandex.qatools.ashot.comparison.ImageDiffer

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class AShotImageDiffer implements Differ {

    private Path root_

    AShotImageDiffer() {}

    AShotImageDiffer(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    void setRoot(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    private static void ensureRoot(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
    }

    DiffArtifact makeDiff(DiffArtifact input) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(input)
        Objects.requireNonNull(input.getExpected())
        Objects.requireNonNull(input.getActual())
        //
        Material expected = input.getExpected()
        if (! expected.isImage()) {
            throw new IllegalArgumentException("the expected material is not an image: ${expected}")
        }
        File expectedFile = root_.resolve(expected.getRelativePath()).toFile()
        BufferedImage expectedImage = readImage(expectedFile)
        assert expectedImage != null
        //
        Material actual = input.getActual()
        if (! actual.isImage()) {
            throw new IllegalArgumentException("the actual material is not an image: ${actual}")
        }
        File actualFile = root_.resolve(actual.getRelativePath()).toFile()
        BufferedImage actualImage = readImage(actualFile)
        assert actualImage != null

        // make a diff image using AShot
        ImageDiffer imgDiff = new ImageDiffer()
        ImageDiff imageDiff = imgDiff.makeDiff(expectedImage,actualImage);
        Metadata diffMetadata = new Metadata([
                "category": "diff",
                "ratio": formatDiffRatioAsString(calculateDiffRatioPercent(imageDiff)),
                "expected": expected.getIndexEntry().getID().toString(),
                "actual": actual.getIndexEntry().getID().toString()
        ])
        byte[] diffData = toByteArray(imageDiff.getDiffImage(), FileType.PNG)
        // write the image diff into disk
        Jobber jobber = new Jobber(root_, actual.getJobName(), actual.getJobTimestamp())
        Material diffMaterial = jobber.write(diffData, FileType.PNG, diffMetadata)

        //
        DiffArtifact result = new DiffArtifact(expected, actual)
        result.setDiff(diffMaterial)
        return result
    }


    private static BufferedImage readImage(File imageFile) {
        if (! imageFile.exists()) {
            throw new IllegalArgumentException("${imageFile} is not found")
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile)
            assert bufferedImage != null
            return bufferedImage
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] toByteArray(BufferedImage input, FileType fileType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(input, fileType.extension, baos);
        byte[] data = baos.toByteArray()
        return data
    }

    /**
     * Calculate the ratio of diff-size against the whole page size.
     *
     * The result is rounded up.  E.g. 0.0001 to 0.01
     *
     * @param diff
     * @return
     */
    private static Double calculateDiffRatioPercent(ImageDiff diff) {
        boolean hasDiff = diff.hasDiff()
        if (!hasDiff) {
            return 0.0
        }
        int diffSize = diff.getDiffSize()
        int area = diff.getMarkedImage().getWidth() * diff.getMarkedImage().getHeight()
        Double diffRatio = diffSize / area * 100
        BigDecimal bd = new BigDecimal(diffRatio)
        BigDecimal bdUP = bd.setScale(2, BigDecimal.ROUND_UP);  // 0.001 -> 0.01
        return bdUP.doubleValue()
    }

    /**
     * @return e.g. "0.23" or "90.00"
     */
    static String formatDiffRatioAsString(Double diffRatio, String fmt = '%1$.2f') {
        return String.format(fmt, diffRatio)
    }
}
