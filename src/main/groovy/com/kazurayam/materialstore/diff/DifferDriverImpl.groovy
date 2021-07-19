package com.kazurayam.materialstore.diff


import com.kazurayam.materialstore.diff.differ.ImageDifferToPNG
import com.kazurayam.materialstore.diff.differ.TextDifferToMarkdown
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Material
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
    List<DiffArtifact> makeDiff(List<DiffArtifact> diffArtifacts) {
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
                    DiffArtifact stuffedDiffArtifact = differ.makeDiff(da)
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
        Builder() {}
        Builder root(Path root) {
            this.root = root
            differs = new HashMap<FileType, Differ>()
            //
            TextDifferToMarkdown javaDiffUtilsTextDiffer = new TextDifferToMarkdown()
            differs.put(FileType.CSV, javaDiffUtilsTextDiffer)
            differs.put(FileType.HTML, javaDiffUtilsTextDiffer)
            differs.put(FileType.JSON, javaDiffUtilsTextDiffer)
            differs.put(FileType.TXT, javaDiffUtilsTextDiffer)
            differs.put(FileType.XML, javaDiffUtilsTextDiffer)
            //
            ImageDifferToPNG aShotImageDiffer = new ImageDifferToPNG()
            differs.put(FileType.PNG, aShotImageDiffer)
            differs.put(FileType.JPG, aShotImageDiffer)
            differs.put(FileType.JPEG, aShotImageDiffer)
            differs.put(FileType.GIF, aShotImageDiffer)
            //
            return this
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
