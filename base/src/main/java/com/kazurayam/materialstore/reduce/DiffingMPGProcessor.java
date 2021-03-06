package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.FileTypeUtil;
import com.kazurayam.materialstore.filesystem.IFileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.differ.Differ;
import com.kazurayam.materialstore.reduce.differ.ImageDifferToPNG;
import com.kazurayam.materialstore.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.reduce.differ.VoidDiffer;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DiffingMPGProcessor implements MPGProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DiffingMPGProcessor.class);
    private final Store store;
    private final Map<IFileType, Differ> differs;

    private DiffingMPGProcessor(Builder builder) {
        this.store = builder.store;
        this.differs = builder.differs;
    }

    // implements Reducer
    @Override
    public MProductGroup process(MProductGroup mProductGroup) throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        final List<MaterialProduct> stuffedMPs = new ArrayList<>();
        for (MaterialProduct input : mProductGroup) {
            // do make difference
            MaterialProduct stuffed = stuffDiffByDiffer(input);
            // memorize the diff
            stuffedMPs.add(stuffed);
        }
        // clone the input to build the result
        final MProductGroup result = new MProductGroup(mProductGroup);
        // and engrave the diff
        for (MaterialProduct stuffed : stuffedMPs) {
            result.update(stuffed);
        }
        result.setReadyToReport(true);
        return result;
    }

    private MaterialProduct stuffDiffByDiffer(final MaterialProduct materialProduct)
            throws MaterialstoreException {
        IFileType fileType;
        if (materialProduct.getLeft().equals(Material.NULL_OBJECT)) {
            logger.warn("left Material was NULL_OBJECT. right=" + materialProduct.getRight());
            fileType = materialProduct.getRight().getIndexEntry().getFileType();
        } else if (materialProduct.getRight().equals(Material.NULL_OBJECT)) {
            logger.warn("right Material was NULL_OBJECT. left=" + materialProduct.getLeft());
            fileType = materialProduct.getLeft().getIndexEntry().getFileType();
        } else {
            fileType = materialProduct.getRight().getIndexEntry().getFileType();
        }
        Differ differ = differs.get(fileType);
        return differ.stuffDiff(materialProduct);
    }

    public boolean hasDiffer(FileType fileType) {
        return differs.containsKey(fileType);
    }

    /**
     *
     */
    public static class Builder {

        private final Store store;
        private final Map<IFileType, Differ> differs;

        public Builder(Store store) {
            Objects.requireNonNull(store);
            this.store = store;
            differs = new HashMap<>();
            //
            final Differ textDiffer = new TextDifferToHTML(store);
            for (IFileType ft : FileTypeUtil.getFileTypesDiffableAsText()) {
                differs.put(ft, textDiffer);
            }
            //
            final Differ imageDiffer = new ImageDifferToPNG(this.store);
            for (IFileType ft : FileTypeUtil.getFileTypesDiffableAsImage()) {
                differs.put(ft, imageDiffer);
            }
            //
            final Differ voidDiffer = new VoidDiffer(this.store);
            for (IFileType ft : FileTypeUtil.getFileTypesUnableToDiff()) {
                differs.put(ft, voidDiffer);
            }
        }

        public Builder differFor(FileType fileType, Differ differ) {
            differs.put(fileType, differ);
            return this;
        }

        public DiffingMPGProcessor build() {
            return new DiffingMPGProcessor(this);
        }
    }
}
