package com.kazurayam.materialstore.resolvent

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.metadata.SortKeys

import java.util.stream.Collectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ArtifactGroup {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactGroup.class)
    private static final boolean verbose = false

    private List<Artifact> artifactList
    private MaterialList leftMaterialList
    private MaterialList rightMaterialList
    private JobTimestamp resolventTimestamp

    private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
    private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    private SortKeys sortKeys = SortKeys.NULL_OBJECT

    /**
     * Deep-copy constructor
     * @param source
     */
    ArtifactGroup(ArtifactGroup source) {
        this.leftMaterialList = new MaterialList(source.leftMaterialList)
        this.rightMaterialList = new MaterialList(source.rightMaterialList)
        this.ignoreMetadataKeys = source.ignoreMetadataKeys    // IgnoreMetadataKeys is immutable
        this.identifyMetadataValues = source.identifyMetadataValues // IdentifyMetadataValues is immutable
        this.sortKeys = source.sortKeys                        // SortKeys is immutable
        List<Artifact> tmp = new ArrayList<>()
        source.artifactList.each { sourceDA ->
            tmp.add(new Artifact(sourceDA))
        }
        this.artifactList = tmp
        this.resolventTimestamp = source.resolventTimestamp
    }

    private ArtifactGroup(Builder builder) {
        this.leftMaterialList = builder.leftMaterialList
        this.rightMaterialList = builder.rightMaterialList
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys
        this.identifyMetadataValues = builder.identifyMetadataValues
        this.sortKeys = builder.sortKeys
        this.resolventTimestamp = builder.resolventTimestamp
        //
        this.artifactList =
                zipMaterials(leftMaterialList, rightMaterialList, this.resolventTimestamp,
                        ignoreMetadataKeys,
                        identifyMetadataValues,
                        sortKeys)
        // at this timing the Artifacts are not yet filled with the diff information, they are still vacant.
    }

    void add(Artifact e) {
        artifactList.add(e)
    }

    boolean update(Artifact e) {
        boolean wasPresent = artifactList.remove(e)
        artifactList.add(e)
        return wasPresent
    }

    int countWarnings(Double criteria) {
        artifactList.stream()
                .filter { Artifact da ->
                    criteria < da.getDiffRatio()
                }
                .collect(Collectors.toList())
                .size()
    }

    Artifact get(int index) {
        return artifactList.get(index)
    }

    JobTimestamp getResolventTimestamp() {
        return this.resolventTimestamp
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

    Iterator<Artifact> iterator() {
        return artifactList.iterator()
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

    int size() {
        return artifactList.size()
    }

    void sort() {
        Collections.sort(artifactList)
    }

    List<QueryOnMetadata> getQueryOnMetadataList() {
        List<QueryOnMetadata> list = new ArrayList<>()
        artifactList.each { Artifact artifact ->
            QueryOnMetadata query = artifact.getQueryOnMetadata()
            QueryOnMetadata deepCopy = QueryOnMetadata.builderWithDeepCopy(query).build()
            list.add(deepCopy)
        }
        return list
    }

    /**
     *
     */
    static List<Artifact> zipMaterials(MaterialList leftList,
                                       MaterialList rightList,
                                       JobTimestamp resolventTimestamp,
                                       IgnoreMetadataKeys ignoreMetadataKeys,
                                       IdentifyMetadataValues identifyMetadataValues,
                                       SortKeys sortKeys) {
        Objects.requireNonNull(leftList)
        Objects.requireNonNull(rightList)
        Objects.requireNonNull(resolventTimestamp)
        Objects.requireNonNull(ignoreMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        Objects.requireNonNull(sortKeys)

        // the result
        List<Artifact> artifactList = new ArrayList<>()

        //
        rightList.each { Material right->
            FileType rightFileType = right.getIndexEntry().getFileType()
            Metadata rightMetadata = right.getIndexEntry().getMetadata()
            QueryOnMetadata rightPattern =
                    QueryOnMetadata.builderWithMetadata(rightMetadata, ignoreMetadataKeys).build()
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
                    Artifact da =
                            new Artifact.Builder(left, right, resolventTimestamp)
                                    .setQueryOnMetadata(rightPattern)
                                    .sortKeys(sortKeys)
                                    .build()
                    artifactList.add(da)
                    sb.append("left metadata: Y ${leftMetadata}\n")
                    foundLeftCount += 1
                } else {
                    sb.append("left metadata: N ${leftMetadata}\n")
                }
            }
            if (foundLeftCount == 0) {
                Artifact da =
                        new Artifact.Builder(Material.NULL_OBJECT, right, resolventTimestamp)
                                .setQueryOnMetadata(rightPattern)
                                .sortKeys(sortKeys)
                                .build()
                artifactList.add(da)
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
            QueryOnMetadata leftPattern =
                    QueryOnMetadata.builderWithMetadata(leftMetadata, ignoreMetadataKeys).build()
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
                    // this must have been found matched already; no need to create a Artifact
                    sb.append("right metadata: Y ${rightMetadata}\n")
                    foundRightCount += 1
                } else {
                    sb.append("right metadata: N ${rightMetadata}\n")
                }
            }
            if (foundRightCount == 0) {
                Artifact da =
                        new Artifact.Builder(left, Material.NULL_OBJECT, resolventTimestamp)
                                .setQueryOnMetadata(leftPattern)
                                .sortKeys(sortKeys)
                                .build()
                artifactList.add(da)
            }
            if (foundRightCount == 0 || foundRightCount >= 2) {
                if (verbose) {
                    logger.warn(sb.toString())
                }
            }
        }
        Collections.sort(artifactList)
        return artifactList
    }

    //---------------------------------------------------------------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        artifactList.each { Artifact da ->
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
        private final List<Artifact> artifactList
        private final MaterialList leftMaterialList
        private final MaterialList rightMaterialList
        private final JobTimestamp resolventTimestamp
        //
        private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        private SortKeys sortKeys = SortKeys.NULL_OBJECT
        //
        Builder(MaterialList left, MaterialList right) {
            this.leftMaterialList = left
            this.rightMaterialList = right
            this.artifactList = new ArrayList<>()
            this.resolventTimestamp = JobTimestamp.laterThan(left.getJobTimestamp(), right.getJobTimestamp())
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
        ArtifactGroup build() {
            return new ArtifactGroup(this)
        }
    }
}
