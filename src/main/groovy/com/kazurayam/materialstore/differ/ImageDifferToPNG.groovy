package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.FileTypeDiffability
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.metadata.Metadata
import ru.yandex.qatools.ashot.comparison.ImageDiff
import ru.yandex.qatools.ashot.comparison.ImageDiffer

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

final class ImageDifferToPNG implements Differ {

    private Path root_

    ImageDifferToPNG() {}

    ImageDifferToPNG(Path root) {
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

    @Override
    Artifact makeArtifact(Artifact artifact) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(artifact)
        Objects.requireNonNull(artifact.getLeft())
        Objects.requireNonNull(artifact.getRight())
        //
        Material left = artifact.getLeft()
        if (! left.getDiffability() == FileTypeDiffability.AS_IMAGE) {
            throw new IllegalArgumentException("the left material is not an image: ${left}")
        }
        File leftFile = root_.resolve(left.getRelativePath()).toFile()
        BufferedImage leftImage = readImage(leftFile)
        assert leftImage != null
        //
        Material right = artifact.getRight()
        if (! right.getDiffability() == FileTypeDiffability.AS_IMAGE) {
            throw new IllegalArgumentException("the right material is not an image: ${right}")
        }
        File rightFile = root_.resolve(right.getRelativePath()).toFile()
        BufferedImage rightImage = readImage(rightFile)
        assert rightImage != null

        // make a diff image using AShot
        ImageDiffer imgDiff = new ImageDiffer()
        ImageDiff imageDiff = imgDiff.makeDiff(leftImage,rightImage);
        Double diffRatio = calculateDiffRatioPercent(imageDiff)
        Metadata diffMetadata = Metadata.builderWithMap([
                "category": "diff",
                "ratio": DifferUtil.formatDiffRatioAsString(diffRatio),
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString()])
                .build()
        byte[] diffData = toByteArray(imageDiff.getDiffImage(), FileType.PNG)
        // write the image diff into disk
        Jobber jobber = new Jobber(root_, right.getJobName(), artifact.getDiffTimestamp())
        Material diffMaterial =
                jobber.write(diffData,
                        FileType.PNG,
                        diffMetadata, Jobber.DuplicationHandling.CONTINUE)

        //
        Artifact result = new Artifact(artifact)
        result.setDiff(diffMaterial)
        result.setDiffRatio(diffRatio)
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
        return DifferUtil.roundUpTo2DecimalPlaces(diffRatio)
    }


}
