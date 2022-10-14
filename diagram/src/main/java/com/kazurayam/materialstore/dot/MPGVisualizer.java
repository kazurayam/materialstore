package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;

import java.awt.image.BufferedImage;
import java.util.Collections;

/**
 * MPGVisualizer generates diagram of an instance of MProductGroup.
 * MPGVisualizer is supposed to be used as a helper class for Selenium test suites.
 * MPGVisualizer internally uses the com.kazurayam.materialstore.dot.DotGenerator class.
 */
public class MPGVisualizer {

    private final Store store;

    public MPGVisualizer(Store store) {
        this.store = store;
    }

    public void visualize(JobName jobName,
                          JobTimestamp jobTimestamp,
                          MaterialProductGroup mProductGroup)
            throws MaterialstoreException {
        //
        String dotBeforeZip = DotGenerator.generateDotOfMPGBeforeZip(
                mProductGroup, Collections.emptyMap(), true);
        BufferedImage imgBeforeZip = DotGenerator.toImage(dotBeforeZip);

        store.write(jobName, jobTimestamp, FileType.DOT,
                Metadata.builder()
                        .put("description",
                                "DOT text of MProductGroup object BEFORE Zipping 2 MaterialList objects")
                        .build(),
                dotBeforeZip);

        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder()
                        .put("description",
                                "Diagram of MProductGroup object BEFORE Zipping 2 MaterialList objects")
                        .build(),
                imgBeforeZip);
        //
        String dotAfterZip = DotGenerator.generateDot(
                mProductGroup, Collections.emptyMap(), true);
        BufferedImage imgAfterZip = DotGenerator.toImage(dotAfterZip);

        store.write(jobName, jobTimestamp, FileType.DOT,
                Metadata.builder()
                        .put("description",
                                "DOT text of MProductGroup object AFTER constructing List<MaterialProduct>")
                        .build(),
                dotAfterZip);

        store.write(jobName, jobTimestamp, FileType.PNG,
                Metadata.builder()
                        .put("description",
                                "Diagram of MProductGroup object AFTER constructing List<MaterialProduct>")
                        .build(),
                imgAfterZip);
    }
}
