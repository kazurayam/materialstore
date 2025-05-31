package com.kazurayam.materialstore.base.reduce.differ;

import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.Jobber;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

public interface Differ {

    Logger logger = LoggerFactory.getLogger(Differ.class);

    default BufferedImage readImage(final Path imageFile) {
        Objects.requireNonNull(imageFile);
        if (!Files.exists(imageFile)) {
            throw new IllegalArgumentException(imageFile + " is not found");
        }
        try {
            InputStream is = new BufferedInputStream(Files.newInputStream(imageFile));
            BufferedImage bufferedImage = ImageIO.read(is);
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

        Jobber jobber = new Jobber(store, mProduct.getJobName(), mProduct.getReducedTimestamp());
        MaterialList noMaterialFoundList =
                jobber.selectMaterials(fileType,
                        QueryOnMetadata.builder(metadata).build());

        logger.debug(String.format("#makeNoMaterialFoundMaterial mProduct.getJobName()=%s", mProduct.getJobName()));
        logger.debug(String.format("#makeNoMaterialFoundMaterial mProduct.getReducedTimestamp()=%s",
                mProduct.getReducedTimestamp()));

        logger.debug(String.format("#makeNoMaterialFoundMaterial noMaterialFoundList(fileType=%s).size()=%d",
                fileType, noMaterialFoundList.size()));

        if (noMaterialFoundList.size() == 0) {
            byte[] imageBytes = bytes;
            Material material =
                    jobber.write(imageBytes, fileType, metadata,
                            Jobber.DuplicationHandling.CONTINUE);
            logger.debug(String.format(
                    "#makeNoMaterialFoundMaterial written a \"No Material is found\" material with id=%s", material.getID()));
            return material;
        } else {
            return noMaterialFoundList.get(0);
        }
    }

    MaterialProduct generateDiff(MaterialProduct mProduct) throws MaterialstoreException;

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
