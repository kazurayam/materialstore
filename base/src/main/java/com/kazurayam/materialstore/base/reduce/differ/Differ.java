package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.Jobber;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public interface Differ {

    default BufferedImage readImage(final File imageFile) {
        if (!imageFile.exists()) {
            throw new IllegalArgumentException(imageFile + " is not found");
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            assert bufferedImage != null;
            return bufferedImage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default Material makeNoMaterialFoundMaterial(
            Store store, MaterialProduct mProduct, FileType fileType, byte[] bytes)
            throws MaterialstoreException {
        Metadata metadata =
                Metadata.builder(Collections.singletonMap("category", "NoMaterialFound")).build();
        MaterialList noMaterialFoundList =
                store.select(mProduct.getJobName(), mProduct.getReducedTimestamp(), FileType.PNG,
                        QueryOnMetadata.builder(metadata).build()) ;
        if (noMaterialFoundList.size() == 0) {
            byte[] imageBytes = bytes;
            Jobber jobber = new Jobber(store, mProduct.getJobName(), mProduct.getReducedTimestamp());
            Material material =
                    jobber.write(imageBytes, fileType, metadata,
                            Jobber.DuplicationHandling.CONTINUE);
            return material;
        } else {
            return noMaterialFoundList.get(0);
        }
    }

    MaterialProduct stuffDiff(MaterialProduct mProduct) throws MaterialstoreException;

    default byte[] toByteArray(BufferedImage input, FileType fileType)
            throws MaterialstoreException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(input, fileType.getExtension(), baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }


}
