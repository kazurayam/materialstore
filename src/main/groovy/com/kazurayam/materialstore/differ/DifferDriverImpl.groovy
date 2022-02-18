package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.diffartifact.DiffArtifacts
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

final class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class)

    private Path root_

    private Map<FileType, Differ> differs_

    private DifferDriverImpl(Builder builder) {
        this.root_ = builder.root;
        this.differs_ = builder.differs
    }

    @Override
    DiffArtifacts differentiate(DiffArtifacts input) {
        Objects.requireNonNull(input)
        Objects.requireNonNull(root_)
        //
        DiffArtifacts stuffed = new DiffArtifacts()
        stuffed.setLeftMaterialList(input.getLeftMaterialList())
        stuffed.setRightMaterialList(input.getRightMaterialList())
        stuffed.setIgnoringMetadataKeys(input.getIgnoringMetadataKeys())
        stuffed.setIdentifyMetadataValues(input.getIdentifyMetadataValues())
        //
        input.each { DiffArtifact da ->
            FileType fileType
            if (da.getLeft() == Material.NULL_OBJECT) {
                logger.warn("left Material was NULL_OBJECT. right=${da.getRight()}")
                fileType = da.getRight().getIndexEntry().getFileType()
            } else if (da.getRight() == Material.NULL_OBJECT) {
                logger.warn("right Material was NULL_OBJECT. left=${da.getLeft()}")
                fileType = da.getLeft().getIndexEntry().getFileType()
            } else {
                fileType = da.getRight().getIndexEntry().getFileType()
            }
            Differ differ = differs_.get(fileType)
            differ.setRoot(root_)
            DiffArtifact stuffedDiffArtifact = differ.makeDiffArtifact(da)
            stuffed.add(stuffedDiffArtifact)
        }
        return stuffed
    }

    @Override
    boolean hasDiffer(FileType fileType) {
        return differs_.containsKey(fileType)
    }

    static class Builder {
        private Path root
        private Map<FileType, Differ> differs
        Builder(Path root) {
            this.root = root
            differs = new HashMap<FileType, Differ>()
            //
            Differ textDiffer = new TextDifferToHTML()
            FileType.getFileTypesDiffableAsText().each {ft ->
                differs.put(ft, textDiffer)
            }
            //
            Differ imageDiffer = new ImageDifferToPNG()
            FileType.getFileTypesDiffableAsImage().each {ft ->
                differs.get(ft, imageDiffer)
            }
            //
            Differ voidDiffer = new VoidDiffer()
            FileType.getFileTypesUnableToDiff().each { ft ->
                differs.get(ft, voidDiffer)
            }
        }
        Builder differFor(FileType fileType, Differ differ) {
            differs.put(fileType, differ)
            return this
        }
        DifferDriverImpl build() {
            return new DifferDriverImpl(this)
        }
    }
}
