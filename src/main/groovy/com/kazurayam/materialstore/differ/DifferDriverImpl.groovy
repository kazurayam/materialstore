package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.diffartifact.DiffArtifact
import com.kazurayam.materialstore.filesystem.Store
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

    /**
     * implements Resolvent
     * @param input
     * @return
     */
    @Override
    DiffArtifactGroup resolve(DiffArtifactGroup input) {
        return differentiate(input)
    }


    @Override
    DiffArtifactGroup differentiate(DiffArtifactGroup input) {
        Objects.requireNonNull(input)
        List<DiffArtifact> differentiated = new ArrayList<>()
        input.each {inputDA ->
            // make the diff info
            DiffArtifact stuffedDA = differentiate(inputDA)
            differentiated.add(stuffedDA)
        }
        // clone the input to build the result
        DiffArtifactGroup result = new DiffArtifactGroup(input)
        differentiated.each {stuffedDA ->
            // update the contents
            result.update(stuffedDA)
        }
        return result
    }

    @Override
    DiffArtifact differentiate(DiffArtifact da) {
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
        return stuffedDiffArtifact
    }

    @Override
    boolean hasDiffer(FileType fileType) {
        return differs_.containsKey(fileType)
    }

    static class Builder {
        private Path root
        private Map<FileType, Differ> differs
        Builder(Store store) {
            this(store.getRoot())
        }
        Builder(Path root) {
            Objects.requireNonNull(root)
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
