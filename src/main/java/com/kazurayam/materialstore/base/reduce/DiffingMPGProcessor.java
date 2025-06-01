package com.kazurayam.materialstore.base.reduce;

import com.kazurayam.materialstore.base.reduce.differ.Differ;
import com.kazurayam.materialstore.base.reduce.differ.ImageDiffStuffer;
import com.kazurayam.materialstore.base.reduce.differ.TextDifferToHTML;
import com.kazurayam.materialstore.base.reduce.differ.VoidDiffer;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.FileTypeUtil;
import com.kazurayam.materialstore.core.IFileType;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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

    // implements MPGProcessor
    @Override
    public MaterialProductGroup process(MaterialProductGroup source) throws MaterialstoreException {
        Objects.requireNonNull(source);
        logger.debug(String.format("#process mpg.getCountTotal()=%d", source.getCountTotal()));

        final List<MaterialProduct> stuffedMaterialProductList = new ArrayList<>();
        for (MaterialProduct input : source) {
            // do make difference and memorize it
            logger.info(String.format("#process %s", input.toString()));
            stuffedMaterialProductList.add(stuffDiffByDiffer(input));
        }
        logger.debug(String.format("#process stuffedMaterialProductList.size()=%d", stuffedMaterialProductList.size()));
        // create a new MaterialProductGroup object with diff info stuffed
        MaterialProductGroup result = new MaterialProductGroup(source, stuffedMaterialProductList);
        result.setReadyToReport(true);
        return result;
    }

    private MaterialProduct stuffDiffByDiffer(final MaterialProduct materialProduct)
            throws MaterialstoreException {
        IFileType fileType;
        if (materialProduct.getLeft().equals(Material.NULL_OBJECT)) {
            logger.warn(String.format("#stuffDiffByDiffer left Material was NULL_OBJECT. right=%s",
                    materialProduct.getRight()));
            fileType = materialProduct.getRight().getIndexEntry().getFileType();
        } else if (materialProduct.getRight().equals(Material.NULL_OBJECT)) {
            logger.warn(String.format("#stuffDiffByDiffer right Material was NULL_OBJECT. left=%s",
                    materialProduct.getLeft()));
            fileType = materialProduct.getLeft().getIndexEntry().getFileType();
        } else {
            fileType = materialProduct.getRight().getIndexEntry().getFileType();
        }
        Differ differ = differs.get(fileType);
        MaterialProduct result = differ.generateDiff(materialProduct);
        return result;
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
        private Color diffColor = Color.RED;

        public Builder(Store store) {
            Objects.requireNonNull(store);
            this.store = store;
            differs = new HashMap<>();
        }

        public Builder differFor(FileType fileType, Differ differ) {
            differs.put(fileType, differ);
            return this;
        }

        public Builder diffColor(Color diffColor) {
            this.diffColor = diffColor;
            return this;
        }

        public DiffingMPGProcessor build() {
            final Differ textDiffer = new TextDifferToHTML(store);
            for (IFileType ft : FileTypeUtil.getFileTypesDiffableAsText()) {
                differs.put(ft, textDiffer);
            }
            //
            final Differ imageDiffer = new ImageDiffStuffer(this.store);
            for (IFileType ft : FileTypeUtil.getFileTypesDiffableAsImage()) {
                differs.put(ft, imageDiffer);
            }
            //
            final Differ voidDiffer = new VoidDiffer(this.store);
            for (IFileType ft : FileTypeUtil.getFileTypesUnableToDiff()) {
                differs.put(ft, voidDiffer);
            }
            return new DiffingMPGProcessor(this);
        }
    }
}
