package com.kazurayam.materialstore.reduce

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.reduce.differ.Differ
import com.kazurayam.materialstore.reduce.differ.ImageDifferToPNG
import com.kazurayam.materialstore.reduce.differ.TextDifferToHTML
import com.kazurayam.materialstore.reduce.differ.VoidDiffer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

final class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class)

    private Path root_

    private Map<FileType, Differ> differs_

    private DifferDriverImpl(Builder builder) {
        this.root_ = builder.root
        this.differs_ = builder.differs
    }

    /**
     * implements Reducer
     * @param input
     * @return
     */
    @Override
    MProductGroup reduce(MProductGroup input) {
        return differentiate(input)
    }


    @Override
    MProductGroup differentiate(MProductGroup mProductGroup) {
        Objects.requireNonNull(mProductGroup)
        List<MProduct> differentiated = new ArrayList<>()
        mProductGroup.each { inputDA ->
            // make the diff info
            MProduct stuffedDA = differentiate(inputDA)
            differentiated.add(stuffedDA)
        }
        // clone the input to build the result
        MProductGroup result = new MProductGroup(mProductGroup)
        differentiated.each {stuffedDA ->
            // update the contents
            result.update(stuffedDA)
        }
        result.setReadyToReport(true)
        return result
    }

    @Override
    MProduct differentiate(MProduct mProduct) {
        FileType fileType
        if (mProduct.getLeft() == Material.NULL_OBJECT) {
            logger.warn("left Material was NULL_OBJECT. right=${mProduct.getRight()}")
            fileType = mProduct.getRight().getIndexEntry().getFileType()
        } else if (mProduct.getRight() == Material.NULL_OBJECT) {
            logger.warn("right Material was NULL_OBJECT. left=${mProduct.getLeft()}")
            fileType = mProduct.getLeft().getIndexEntry().getFileType()
        } else {
            fileType = mProduct.getRight().getIndexEntry().getFileType()
        }
        Differ differ = differs_.get(fileType)
        differ.setRoot(root_)
        MProduct stuffed = differ.makeMProduct(mProduct)
        return stuffed
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
            FileType.getFileTypesDiffableAsText().each { ft ->
                differs.put(ft, textDiffer)
            }
            //
            Differ imageDiffer = new ImageDifferToPNG()
            FileType.getFileTypesDiffableAsImage().each { ft ->
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
