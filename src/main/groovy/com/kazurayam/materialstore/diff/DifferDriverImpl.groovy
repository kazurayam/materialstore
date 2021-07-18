package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.diff.differ.AShotImageDiffer
import com.kazurayam.materialstore.diff.differ.JavaDiffUtilsTextDiffer
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Material
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class DifferDriverImpl implements DifferDriver {

    private static final Logger logger = LoggerFactory.getLogger(DifferDriverImpl.class)

    private Path root_

    private Map<FileType, Differ> associations_

    private DifferDriverImpl(Builder builder) {
        this.root_ = builder.root;
        this.associations_ = builder.associations
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
                if (associations_.containsKey(fileType)) {
                    Differ differ = associations_.get(fileType)
                    differ.setRoot(root_)
                    DiffArtifact stuffedDiffArtifact = differ.makeDiff(da)
                    results.add(stuffedDiffArtifact)
                } else {
                    logger.warn("FileType ${fileType.getExtension()} is not supported yet." +
                            " in ${da.getActual().toString()}")
                }
                /*
                switch (fileType) {
                    case (FileType.HTML):
                        Differ textDiffer = new JavaDiffUtilsTextDiffer(root_)
                        results.add(textDiffer.makeDiff(da))
                        break
                    case (FileType.PNG):
                        Differ imageDiffer = new AShotImageDiffer(root_)
                        results.add(imageDiffer.makeDiff(da))
                        break
                    default:
                        logger.warn("FileType ${fileType.getExtension()} is not supported yet." +
                                " in ${da.getActual().toString()}")
                }
                 */
            }
        }
        return results
    }

    @Override
    boolean hasDiffer(FileType fileType) {
        return associations_.containsKey(fileType)
    }

    static class Builder {
        private Path root
        private Map<FileType, Differ> associations
        Builder() {}
        Builder root(Path root) {
            this.root = root
            associations = new HashMap<FileType, Differ>()
            //
            JavaDiffUtilsTextDiffer javaDiffUtilsTextDiffer = new JavaDiffUtilsTextDiffer()
            associations.put(FileType.CSV, javaDiffUtilsTextDiffer)
            associations.put(FileType.HTML, javaDiffUtilsTextDiffer)
            associations.put(FileType.JSON, javaDiffUtilsTextDiffer)
            associations.put(FileType.TXT, javaDiffUtilsTextDiffer)
            associations.put(FileType.XML, javaDiffUtilsTextDiffer)
            //
            AShotImageDiffer aShotImageDiffer = new AShotImageDiffer()
            associations.put(FileType.PNG, aShotImageDiffer)
            associations.put(FileType.JPG, aShotImageDiffer)
            associations.put(FileType.JPEG, aShotImageDiffer)
            associations.put(FileType.GIF, aShotImageDiffer)
            //
            return this
        }
        Builder differFor(FileType fileType, Differ differ) {
            associations.put(fileType, differ)
            return this
        }
        DifferDriverImpl build() {
            return new DifferDriverImpl(this)
        }
    }
}
