package com.kazurayam.materialstore.store


import com.kazurayam.materialstore.store.differ.ImageDifferToPNG
import com.kazurayam.materialstore.store.differ.TextDifferToHTML
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class)

    private Path root_

    private Map<FileType, Differ> differs_

    private DifferDriverImpl(Builder builder) {
        this.root_ = builder.root;
        this.differs_ = builder.differs
    }

    @Override
    List<DiffArtifact> makeDiffArtifacts(List<DiffArtifact> diffArtifacts) {
        Objects.requireNonNull(diffArtifacts)
        Objects.requireNonNull(root_)
        List<DiffArtifact> results = new ArrayList<DiffArtifact>()
        diffArtifacts.each { DiffArtifact da ->
            if (da.getExpected() == Material.NULL_OBJECT) {
                logger.warn("expected Material was NULL_OBJECT. actual=${da.getActual()}")
            } else if (da.getActual() == Material.NULL_OBJECT) {
                logger.warn("actual Material was NULL_OBJECT. expected=${da.getExpected()}")
            } else {
                FileType fileType = da.getActual().getIndexEntry().getFileType()
                if (differs_.containsKey(fileType)) {
                    Differ differ = differs_.get(fileType)
                    differ.setRoot(root_)
                    DiffArtifact stuffedDiffArtifact = differ.makeDiffArtifact(da)
                    results.add(stuffedDiffArtifact)
                } else {
                    logger.warn("FileType ${fileType.getExtension()} is not supported yet." +
                            " in ${da.getActual().toString()}")
                }
            }
        }
        return results
    }

    @Override
    boolean hasDiffer(FileType fileType) {
        return differs_.containsKey(fileType)
    }

    static class Builder {
        private Path root
        private Map<FileType, Differ> differs
        Builder(Path root) {
            this.root = root
            differs = new HashMap<FileType, Differ>()
            //
            Differ textDiffer = new TextDifferToHTML()
            differs.put(FileType.CSV, textDiffer)
            differs.put(FileType.HTML, textDiffer)
            differs.put(FileType.JSON, textDiffer)
            differs.put(FileType.TXT, textDiffer)
            differs.put(FileType.XML, textDiffer)
            //
            Differ imageDiffer = new ImageDifferToPNG()
            differs.put(FileType.PNG, imageDiffer)
            differs.put(FileType.JPG, imageDiffer)
            differs.put(FileType.JPEG, imageDiffer)
            differs.put(FileType.GIF, imageDiffer)
            //
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
