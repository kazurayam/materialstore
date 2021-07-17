package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import ru.yandex.qatools.ashot.comparison.ImageDiff

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class ImageDiffer implements Differ {

    private Path root_

    ImageDiffer() {}

    ImageDiffer(Path root) {
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
        Material expected = input.getExpected()
        byte[] expectedData = readMaterial(root_, expected)
        BufferedImage expectedImage = createImageFromBytes(expectedData)
        assert expectedImage != null
        Material actual = input.getActual()
        byte[] actualData = readMaterial(root_, actual)
        BufferedImage actualImage = createImageFromBytes(actualData)
        assert actualImage != null

        // make a diff image using AShot
        ru.yandex.qatools.ashot.comparison.ImageDiffer imgDiff = new ru.yandex.qatools.ashot.comparison.ImageDiffer()
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

    private static byte[] readMaterial(Path root, Material material) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(material)
        Jobber jobber = new Jobber(root, material.getJobName(), material.getJobTimestamp())
        return jobber.read(material.getIndexEntry())
    }

    private static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
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
