package com.kazurayam.materialstore.diffartifact

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.MetadataPattern
import com.kazurayam.materialstore.metadata.SortKeys

import java.util.stream.Collectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class DiffArtifactGroup {

    private static final Logger logger = LoggerFactory.getLogger(DiffArtifactGroup.class)
    private static final boolean verbose = false

    private List<DiffArtifact> diffArtifactList
    private MaterialList leftMaterialList
    private MaterialList rightMaterialList
    private JobTimestamp diffTimestamp

    private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
    private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    private SortKeys sortKeys = SortKeys.NULL_OBJECT


    private DiffArtifactGroup() {
        diffArtifactList = new ArrayList<DiffArtifact>()
    }

    /**
     * Deep-copy constructor
     * @param source
     */
    public DiffArtifactGroup(DiffArtifactGroup source) {
        this.leftMaterialList = new MaterialList(source.leftMaterialList)
        this.rightMaterialList = new MaterialList(source.rightMaterialList)
        this.ignoreMetadataKeys = source.ignoreMetadataKeys    // IgnoreMetadataKeys is immutable
        this.identifyMetadataValues = source.identifyMetadataValues // IdentifyMetadataValues is immutable
        this.sortKeys = source.sortKeys                        // SortKeys is immutable
        List<DiffArtifact> tmp = new ArrayList<>()
        source.diffArtifactList.each {sourceDA ->
            tmp.add(new DiffArtifact(sourceDA))
        }
        this.diffArtifactList = tmp
        this.diffTimestamp = source.diffTimestamp
    }

    private DiffArtifactGroup(Builder builder) {
        this.leftMaterialList = builder.leftMaterialList
        this.rightMaterialList = builder.rightMaterialList
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys
        this.identifyMetadataValues = builder.identifyMetadataValues
        this.sortKeys = builder.sortKeys
        this.diffTimestamp = builder.diffTimestamp
        //
        this.diffArtifactList =
                zipMaterials(leftMaterialList, rightMaterialList, diffTimestamp,
                        ignoreMetadataKeys,
                        identifyMetadataValues,
                        sortKeys)
        // at this timing the DiffArtifacts are not yet filled with the diff information, they are still vacant.
    }

    void add(DiffArtifact e) {
        diffArtifactList.add(e)
    }

    boolean update(DiffArtifact e) {
        boolean wasPresent = diffArtifactList.remove(e)
        diffArtifactList.add(e)
        return wasPresent
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

    JobTimestamp getDiffTimestamp() {
        return diffTimestamp
    }

    IdentifyMetadataValues getIdentifyMetadataValues() {
        return this.identifyMetadataValues
    }

    IgnoreMetadataKeys getIgnoreMetadataKeys() {
        return this.ignoreMetadataKeys
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

    void setIgnoreMetadataKeys(IgnoreMetadataKeys ignoreMetadataKeys) {
        this.ignoreMetadataKeys = ignoreMetadataKeys
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
                                           JobTimestamp diffTimestamp,
                                           IgnoreMetadataKeys ignoreMetadataKeys,
                                           IdentifyMetadataValues identifyMetadataValues,
                                           SortKeys sortKeys) {
        Objects.requireNonNull(leftList)
        Objects.requireNonNull(rightList)
        Objects.requireNonNull(diffTimestamp)
        Objects.requireNonNull(ignoreMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        Objects.requireNonNull(sortKeys)

        // the result
        List<DiffArtifact> diffArtifactList = new ArrayList<>()

        //
        rightList.each { Material right->
            FileType rightFileType = right.getIndexEntry().getFileType()
            Metadata rightMetadata = right.getIndexEntry().getMetadata()
            MetadataPattern rightPattern =
                    MetadataPattern.builderWithMetadata(rightMetadata, ignoreMetadataKeys).build()
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
                            new DiffArtifact.Builder(left, right, diffTimestamp)
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
                    MetadataPattern.builderWithMetadata(leftMetadata, ignoreMetadataKeys).build()
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

    static Builder builder(MaterialList left, MaterialList right) {
        return new Builder(left, right)
    }

    /**
     *
     */
    private static class Builder {
        // required
        private final List<DiffArtifact> diffArtifactList
        private final MaterialList leftMaterialList
        private final MaterialList rightMaterialList
        private final JobTimestamp diffTimestamp
        //
        private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        private SortKeys sortKeys = SortKeys.NULL_OBJECT
        //
        Builder(MaterialList left, MaterialList right) {
            this.leftMaterialList = left
            this.rightMaterialList = right
            this.diffArtifactList = new ArrayList<>()
            this.diffTimestamp = JobTimestamp.laterThan(left.getJobTimestamp(), right.getJobTimestamp())
        }
        Builder ignoreKeys(String ... keys) {
            IgnoreMetadataKeys imk =
                    new IgnoreMetadataKeys.Builder().ignoreKeys(keys).build()
            return setIgnoreMetadataKeys(imk)
        }
        Builder setIgnoreMetadataKeys(IgnoreMetadataKeys ignoreMetadataKeys) {
            this.ignoreMetadataKeys = ignoreMetadataKeys
            return this
        }
        Builder identifyWithRegex(Map<String, String> pairs) {
            IdentifyMetadataValues imv =
                    new IdentifyMetadataValues.Builder().putAllNameRegexPairs(pairs).build()
            return setIdentifyMetadataValues(imv)
        }
        Builder setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
            this.identifyMetadataValues = identifyMetadataValues
            return this
        }
        Builder sort(String ... args) {
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
