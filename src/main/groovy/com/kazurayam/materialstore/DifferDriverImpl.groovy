package com.kazurayam.materialstore


import com.kazurayam.materialstore.differ.ImageDifferToPNG
import com.kazurayam.materialstore.differ.TextDifferToHTML
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class DifferDriverImpl implements DifferDriver {

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
        DiffArtifacts stuffed = new DiffArtifacts()
        input.each { DiffArtifact da ->
            if (da.getLeft() == Material.NULL_OBJECT) {
                logger.warn("left Material was NULL_OBJECT. riight=${da.getRight()}")
            } else if (da.getRight() == Material.NULL_OBJECT) {
                logger.warn("right Material was NULL_OBJECT. left=${da.getLeft()}")
            } else {
                FileType fileType = da.getRight().getIndexEntry().getFileType()
                if (differs_.containsKey(fileType)) {
                    Differ differ = differs_.get(fileType)
                    differ.setRoot(root_)
                    DiffArtifact stuffedDiffArtifact = differ.makeDiffArtifact(da)
                    stuffed.add(stuffedDiffArtifact)
                } else {
                    logger.warn("FileType ${fileType.getExtension()} is not supported yet." +
                            " in ${da.getRight().toString()}")
                }
            }
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
            differs.put(FileType.CSV, textDiffer)
            differs.put(FileType.HTML, textDiffer)
            differs.put(FileType.JSON, textDiffer)
            differs.put(FileType.TXT, textDiffer)
            differs.put(FileType.XML, textDiffer)
            //
            Differ imageDiffer = new ImageDifferToPNG()
            differs.put(FileType.PNG, imageDiffer)
            differs.put(FileType.JPG, imageDiffer)
            differs.put(FileType.JPEG, imageDiffer)
            differs.put(FileType.GIF, imageDiffer)
            //
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
