package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Material
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriver.class)

    private DifferDriver() {}

    static List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts) {
        Objects.requireNonNull(diffArtifacts)
        List<DiffArtifact> results = new ArrayList<DiffArtifact>()
        diffArtifacts.each { DiffArtifact da ->
            if (da.getExpected() == Material.NULL_OBJECT) {
                logger.warn("expected Material was NULL_OBJECT")
            } else if (da.getActual() == Material.NULL_OBJECT) {
                logger.warn("actual Material was NULL_OBJECT")
            } else {
                FileType fileType = da.getActual().getIndexEntry().getFileType()
                switch (fileType) {
                    case (FileType.HTML):
                        results.add(new HTMLDiffer().makeDiff(da))
                        break
                    case (FileType.PNG):
                        results.add(new PNGDiffer().makeDiff(da))
                        break
                    default:
                        logger.warn("FileType ${fileType.getExtension()} is not supported yet." +
                                " in ${da.getActual().toString()}")
                }
            }
        }
    }

}
