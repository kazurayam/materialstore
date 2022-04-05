package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.differ.Differ;
import com.kazurayam.materialstore.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.reduce.differ.VoidDiffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class);
    private final Store store;
    private final Map<FileType, Differ> differs;

    private DifferDriverImpl(Builder builder) {
        this.store = builder.store;
        this.differs = builder.differs;
    }

    // implements Reducer
    @Override
    public MProductGroup reduce(MProductGroup input) throws MaterialstoreException {
        return differentiate(input);
    }

    @Override
    public MProductGroup differentiate(MProductGroup mProductGroup) throws MaterialstoreException {
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
    public MaterialProduct differentiate(final MaterialProduct mProduct) throws MaterialstoreException {
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

        Differ differ = differs.get(fileType);
        differ.setStore(store);
        return differ.injectDiff(mProduct);
    }

    @Override
    public boolean hasDiffer(FileType fileType) {
        return differs.containsKey(fileType);
    }

    /**
     *
     */
    public static class Builder {

        private final Store store;
        private final Map<FileType, Differ> differs;

        public Builder(Store store) {
            Objects.requireNonNull(store);
            this.store = store;
            differs = new HashMap<>();
            //
            //final Differ textDiffer = new TextDifferToHTMLMB();
            final Differ textDiffer = new TextDifferToHTML(store);
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
