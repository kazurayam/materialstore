package com.kazurayam.materialstore.diffartifact

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.MetadataPattern

import java.util.stream.Collectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class DiffArtifactGroup {

    private static final Logger logger = LoggerFactory.getLogger(DiffArtifactGroup.class)
    private static final boolean verbose = false

    private final List<DiffArtifact> diffArtifactList
    private MaterialList leftMaterialList
    private MaterialList rightMaterialList

    private IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT
    private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    private SortKeys sortKeys = SortKeys.NULL_OBJECT


    DiffArtifactGroup() {
        diffArtifactList = new ArrayList<DiffArtifact>()
    }

    private DiffArtifactGroup(Builder builder) {
        this.leftMaterialList = builder.leftMaterialList
        this.rightMaterialList = builder.rightMaterialList
        this.ignoringMetadataKeys = builder.ignoringMetadataKeys
        this.identifyMetadataValues = builder.identifyMetadataValues
        this.sortKeys = builder.sortKeys
        //
        this.diffArtifactList =
                zipMaterials(leftMaterialList, rightMaterialList,
                        ignoringMetadataKeys,
                        identifyMetadataValues,
                        sortKeys)
    }

    void add(DiffArtifact e) {
        diffArtifactList.add(e)
    }

    int countWarnings(Double criteria) {
        diffArtifactList.stream()
                .filter { DiffArtifact da ->
                    criteria < da.getDiffRatio()
                }
                .collect(Collectors.toList())
                .size()
    }

    DiffArtifact get(int index) {
        return diffArtifactList.get(index)
    }

    IdentifyMetadataValues getIdentifyMetadataValues() {
        return this.identifyMetadataValues
    }

    IgnoringMetadataKeys getIgnoringMetadataKeys() {
        return this.ignoringMetadataKeys
    }

    MaterialList getLeftMaterialList() {
        return this.leftMaterialList
    }

    MaterialList getRightMaterialList() {
        return this.rightMaterialList
    }

    SortKeys getSortKeys() {
        return this.sortKeys
    }

    Iterator<DiffArtifact> iterator() {
        return diffArtifactList.iterator()
    }

    void setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
        this.identifyMetadataValues = identifyMetadataValues
    }

    void setIgnoringMetadataKeys(IgnoringMetadataKeys ignoringMetadataKeys) {
        this.ignoringMetadataKeys = ignoringMetadataKeys
    }

    void setLeftMaterialList(MaterialList materialList) {
        this.leftMaterialList = materialList
    }

    void setRightMaterialList(MaterialList materialList) {
        this.rightMaterialList = materialList
    }

    void setSortKeys(SortKeys sortKeys) {
        this.sortKeys = sortKeys
    }

    int size() {
        return diffArtifactList.size()
    }

    void sort() {
        Collections.sort(diffArtifactList)
    }

    List<MetadataPattern> getMetadataPatterns() {
        List<MetadataPattern> list = new ArrayList<>()
        diffArtifactList.each { DiffArtifact da ->
            MetadataPattern mp = da.getDescriptor()
            MetadataPattern deepCopy = new MetadataPattern.Builder(mp).build()
            list.add(deepCopy)
        }
        return list
    }

    /**
     *
     */
    static List<DiffArtifact> zipMaterials(MaterialList leftList,
                                           MaterialList rightList,
                                           IgnoringMetadataKeys ignoringMetadataKeys,
                                           IdentifyMetadataValues identifyMetadataValues,
                                           SortKeys sortKeys) {
        Objects.requireNonNull(leftList)
        Objects.requireNonNull(rightList)
        Objects.requireNonNull(ignoringMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        Objects.requireNonNull(sortKeys)

        // the result
        List<DiffArtifact> diffArtifactList = new ArrayList<>()

        //
        rightList.each { Material right->
            FileType rightFileType = right.getIndexEntry().getFileType()
            Metadata rightMetadata = right.getIndexEntry().getMetadata()
            MetadataPattern rightPattern =
                    MetadataPattern.builderWithMetadata(rightMetadata, ignoringMetadataKeys).build()
           //
            StringBuilder sb = new StringBuilder()  // to compose a log message
            sb.append("\nright pattern: ${rightPattern}\n")
            int foundLeftCount = 0
            leftList.each { Material left ->
                FileType leftFileType = left.getIndexEntry().getFileType()
                Metadata leftMetadata = left.getIndexEntry().getMetadata()
                if (leftFileType == rightFileType &&
                        ( rightPattern.matches(leftMetadata) ||
                                identifyMetadataValues.matches(leftMetadata) )
                ) {
                    DiffArtifact da =
                            new DiffArtifact.Builder(left, right)
                                    .setMetadataPattern(rightPattern)
                                    .sortKeys(sortKeys)
                                    .build()
                    diffArtifactList.add(da)
                    sb.append("left metadata: Y ${leftMetadata}\n")
                    foundLeftCount += 1
                } else {
                    sb.append("left metadata: N ${leftMetadata}\n")
                }
            }
            if (foundLeftCount == 0) {
                DiffArtifact da =
                        new DiffArtifact.Builder(Material.NULL_OBJECT, right)
                                .setMetadataPattern(rightPattern)
                                .sortKeys(sortKeys)
                                .build()
                diffArtifactList.add(da)
            }
            if (foundLeftCount == 0 || foundLeftCount >= 2) {
                if (verbose) {
                    logger.warn(sb.toString())
                }
            }
        }

        //
        leftList.each { Material left ->
            FileType leftFileType = left.getIndexEntry().getFileType()
            Metadata leftMetadata = left.getIndexEntry().getMetadata()
            MetadataPattern leftPattern =
                    MetadataPattern.builderWithMetadata(leftMetadata, ignoringMetadataKeys).build()
            StringBuilder sb = new StringBuilder()  // to compose a log message
            sb.append("\nleft pattern: ${leftPattern}\n")
            int foundRightCount = 0
            rightList.each { Material right ->
                FileType rightFileType = right.getIndexEntry().getFileType()
                Metadata rightMetadata = right.getIndexEntry().getMetadata()
                if (rightFileType == leftFileType &&
                        ( leftPattern.matches(rightMetadata) ||
                                identifyMetadataValues.matches(rightMetadata) )
                ) {
                    // this must have been found matched already; no need to create a DiffArtifact
                    sb.append("right metadata: Y ${rightMetadata}\n")
                    foundRightCount += 1
                } else {
                    sb.append("right metadata: N ${rightMetadata}\n")
                }
            }
            if (foundRightCount == 0) {
                DiffArtifact da =
                        new DiffArtifact.Builder(left, Material.NULL_OBJECT)
                                .setMetadataPattern(leftPattern)
                                .sortKeys(sortKeys)
                                .build()
                diffArtifactList.add(da)
            }
            if (foundRightCount == 0 || foundRightCount >= 2) {
                if (verbose) {
                    logger.warn(sb.toString())
                }
            }
        }
        Collections.sort(diffArtifactList)
        return diffArtifactList
    }

    //---------------------------------------------------------------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        diffArtifactList.each { DiffArtifact da ->
            if (count > 0) sb.append(",")
            sb.append(da.toString())
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }

    /**
     *
     */
    static class Builder {
        // required
        private final List<DiffArtifact> diffArtifactList
        private final MaterialList leftMaterialList
        private final MaterialList rightMaterialList
        //
        private IgnoringMetadataKeys ignoringMetadataKeys = IgnoringMetadataKeys.NULL_OBJECT
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        private SortKeys sortKeys = SortKeys.NULL_OBJECT
        //
        Builder() {
            this.leftMaterialList
        }
        Builder(MaterialList left, MaterialList right) {
            this.leftMaterialList = left
            this.rightMaterialList = right
            this.diffArtifactList = new ArrayList<>()
        }
        Builder ignoreKeys(String ... keys) {
            IgnoringMetadataKeys imk =
                    new IgnoringMetadataKeys.Builder().ignoreKeys(keys).build()
            return setIgnoringMetadataKeys(imk)
        }
        Builder setIgnoringMetadataKeys(IgnoringMetadataKeys ignoringMetadataKeys) {
            this.ignoringMetadataKeys = ignoringMetadataKeys
            return this
        }
        Builder identifyWithRegex(Map<String, String> pairs) {
            IdentifyMetadataValues imv =
                    new IdentifyMetadataValues.Builder().putAll(pairs).build()
            return setIdentifyMetadataValues(imv)
        }
        Builder setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
            this.identifyMetadataValues = identifyMetadataValues
            return this
        }
        Builder sortByKeys(String ... args) {
            SortKeys sortKeys = new SortKeys(args)
            return this.setSortKeys(sortKeys)
        }
        Builder setSortKeys(SortKeys sortKeys) {
            this.sortKeys = sortKeys
            return this
        }
        DiffArtifactGroup build() {
            return new DiffArtifactGroup(this)
        }
    }
}
