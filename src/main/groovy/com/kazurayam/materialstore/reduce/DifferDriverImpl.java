package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.differ.Differ;
import com.kazurayam.materialstore.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.reduce.differ.VoidDiffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class);
    private final Path root_;
    private final Map<FileType, Differ> differs_;

    private DifferDriverImpl(Builder builder) {
        this.root_ = builder.root;
        this.differs_ = builder.differs;
    }

    // implements Reducer
    @Override
    public MProductGroup reduce(MProductGroup input) {
        return differentiate(input);
    }

    @Override
    public MProductGroup differentiate(MProductGroup mProductGroup) {
        Objects.requireNonNull(mProductGroup);
        final List<MaterialProduct> differentiated = new ArrayList<>();
        Iterator<MaterialProduct> iter = mProductGroup.iterator();
        while (iter.hasNext()) {
            MaterialProduct input = iter.next();
            // do make difference
            MaterialProduct stuffed = differentiate(input);
            // memorize the diff
            differentiated.add(stuffed);
        }
        // clone the input to build the result
        final MProductGroup result = new MProductGroup(mProductGroup);
        // and engrave the diff
        for (MaterialProduct stuffed : differentiated) {
            result.update(stuffed);
        }
        result.setReadyToReport(true);
        return result;
    }

    @Override
    public MaterialProduct differentiate(final MaterialProduct mProduct) {
        FileType fileType;
        if (mProduct.getLeft().equals(Material.NULL_OBJECT)) {
            logger.warn("left Material was NULL_OBJECT. right=" + mProduct.getRight());
            fileType = mProduct.getRight().getIndexEntry().getFileType();
        } else if (mProduct.getRight().equals(Material.NULL_OBJECT)) {
            logger.warn("right Material was NULL_OBJECT. left=" + mProduct.getLeft());
            fileType = mProduct.getLeft().getIndexEntry().getFileType();
        } else {
            fileType = mProduct.getRight().getIndexEntry().getFileType();
        }

        Differ differ = differs_.get(fileType);
        differ.setRoot(root_);
        return differ.makeMProduct(mProduct);
    }

    @Override
    public boolean hasDiffer(FileType fileType) {
        return differs_.containsKey(fileType);
    }

    /**
     *
     */
    public static class Builder {

        private final Path root;
        private final Map<FileType, Differ> differs;

        public Builder(Store store) {
            this(store.getRoot());
        }

        public Builder(Path root) {
            Objects.requireNonNull(root);
            this.root = root;
            differs = new HashMap<>();
            //
            final Differ textDiffer = new TextDifferToHTML();
            for (FileType ft : FileType.getFileTypesDiffableAsText()) {
                differs.put(ft, textDiffer);
            }
            //
            final Differ imageDiffer = new ImageDifferToPNG();
            for (FileType ft : FileType.getFileTypesDiffableAsImage()) {
                differs.put(ft, imageDiffer);
            }
            //
            final Differ voidDiffer = new VoidDiffer();
            for (FileType ft : FileType.getFileTypesUnableToDiff()) {
                differs.put(ft, voidDiffer);
            }
        }

        public Builder differFor(FileType fileType, Differ differ) {
            differs.put(fileType, differ);
            return this;
        }

        public DifferDriverImpl build() {
            return new DifferDriverImpl(this);
        }
    }
}
