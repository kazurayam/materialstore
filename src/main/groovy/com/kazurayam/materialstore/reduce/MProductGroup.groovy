package com.kazurayam.materialstore.reduce

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

final class MProductGroup {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroup.class)
    private static final boolean verbose = false

    private List<MProduct> mProductList
    private MaterialList materialList0
    private MaterialList materialList1
    private JobTimestamp resultTimestamp

    private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
    private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
    private SortKeys sortKeys = SortKeys.NULL_OBJECT

    /**
     * Deep-copy constructor
     * @param source
     */
    MProductGroup(MProductGroup source) {
        this.materialList0 = new MaterialList(source.materialList0)
        this.materialList1 = new MaterialList(source.materialList1)
        this.ignoreMetadataKeys = source.ignoreMetadataKeys    // IgnoreMetadataKeys is immutable
        this.identifyMetadataValues = source.identifyMetadataValues // IdentifyMetadataValues is immutable
        this.sortKeys = source.sortKeys                        // SortKeys is immutable
        List<MProduct> tmp = new ArrayList<>()
        source.mProductList.each { sourceDA ->
            tmp.add(new MProduct(sourceDA))
        }
        this.mProductList = tmp
        this.resultTimestamp = source.resultTimestamp
    }

    private MProductGroup(Builder builder) {
        this.materialList0 = builder.materialList0
        this.materialList1 = builder.materialList1
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys
        this.identifyMetadataValues = builder.identifyMetadataValues
        this.sortKeys = builder.sortKeys
        this.resultTimestamp = builder.resolventTimestamp
        //
        this.mProductList =
                zipMaterials(materialList0, materialList1, this.resultTimestamp,
                        ignoreMetadataKeys,
                        identifyMetadataValues,
                        sortKeys)
        // at this timing the MProducts are not yet filled with the diff information, they are still vacant.
    }

    void add(MProduct e) {
        mProductList.add(e)
    }

    boolean update(MProduct e) {
        boolean wasPresent = mProductList.remove(e)
        mProductList.add(e)
        return wasPresent
    }

    int countWarnings(Double criteria) {
        mProductList.stream()
                .filter { MProduct da ->
                    criteria < da.getDiffRatio()
                }
                .collect(Collectors.toList())
                .size()
    }

    MProduct get(int index) {
        return mProductList.get(index)
    }

    JobTimestamp getResultTimestamp() {
        return this.resultTimestamp
    }

    IdentifyMetadataValues getIdentifyMetadataValues() {
        return this.identifyMetadataValues
    }

    JobTimestamp getJobTimestampLeft() {
        return this.getMaterialListLeft().getJobTimestamp()
    }

    JobTimestamp getJobTimestampRight() {
        return this.getMaterialListRight().getJobTimestamp()
    }

    JobTimestamp getJobTimestampPrevious() {
        return this.getMaterialListPrevious().getJobTimestamp()
    }

    JobTimestamp getJobTimestampFollowing() {
        return this.getMaterialListFollowing().getJobTimestamp()
    }

    IgnoreMetadataKeys getIgnoreMetadataKeys() {
        return this.ignoreMetadataKeys
    }

    MaterialList getMaterialListLeft() {
        return this.materialList0
    }

    MaterialList getMaterialListRight() {
        return this.materialList1
    }

    MaterialList getMaterialListPrevious() {
        return this.materialList0
    }

    MaterialList getMaterialListFollowing() {
        return this.materialList1
    }

    SortKeys getSortKeys() {
        return this.sortKeys
    }

    Iterator<MProduct> iterator() {
        return mProductList.iterator()
    }

    void setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
        this.identifyMetadataValues = identifyMetadataValues
    }

    void setIgnoreMetadataKeys(IgnoreMetadataKeys ignoreMetadataKeys) {
        this.ignoreMetadataKeys = ignoreMetadataKeys
    }

    void setMaterialListLeft(MaterialList materialList) {
        this.materialList0 = materialList
    }

    void setMaterialListRight(MaterialList materialList) {
        this.materialList1 = materialList
    }

    int size() {
        return mProductList.size()
    }

    void sort() {
        Collections.sort(mProductList)
    }

    List<QueryOnMetadata> getQueryOnMetadataList() {
        List<QueryOnMetadata> list = new ArrayList<>()
        mProductList.each { MProduct mProduct ->
            QueryOnMetadata query = mProduct.getQueryOnMetadata()
            QueryOnMetadata deepCopy = QueryOnMetadata.builder(query).build()
            list.add(deepCopy)
        }
        return list
    }

    /**
     *
     */
    static List<MProduct> zipMaterials(MaterialList leftList,
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
        List<MProduct> mProductList = new ArrayList<>()

        //
        rightList.each { Material right->
            FileType rightFileType = right.getIndexEntry().getFileType()
            Metadata rightMetadata = right.getIndexEntry().getMetadata()
            QueryOnMetadata rightPattern =
                    QueryOnMetadata.builder(rightMetadata, ignoreMetadataKeys).build()
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
                    MProduct da =
                            new MProduct.Builder(left, right, resolventTimestamp)
                                    .setQueryOnMetadata(rightPattern)
                                    .sortKeys(sortKeys)
                                    .build()
                    mProductList.add(da)
                    sb.append("left metadata: Y ${leftMetadata}\n")
                    foundLeftCount += 1
                } else {
                    sb.append("left metadata: N ${leftMetadata}\n")
                }
            }
            if (foundLeftCount == 0) {
                MProduct da =
                        new MProduct.Builder(Material.NULL_OBJECT, right, resolventTimestamp)
                                .setQueryOnMetadata(rightPattern)
                                .sortKeys(sortKeys)
                                .build()
                mProductList.add(da)
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
                    QueryOnMetadata.builder(leftMetadata, ignoreMetadataKeys).build()
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
                    // this must have been found matched already; no need to create a MProduct
                    sb.append("right metadata: Y ${rightMetadata}\n")
                    foundRightCount += 1
                } else {
                    sb.append("right metadata: N ${rightMetadata}\n")
                }
            }
            if (foundRightCount == 0) {
                MProduct da =
                        new MProduct.Builder(left, Material.NULL_OBJECT, resolventTimestamp)
                                .setQueryOnMetadata(leftPattern)
                                .sortKeys(sortKeys)
                                .build()
                mProductList.add(da)
            }
            if (foundRightCount == 0 || foundRightCount >= 2) {
                if (verbose) {
                    logger.warn(sb.toString())
                }
            }
        }
        Collections.sort(mProductList)
        return mProductList
    }

    //---------------------------------------------------------------
    @Override
    String toString() {
        return getDescription(true)
    }

    String getDescription(boolean fullContent=false) {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"resultTimestamp\":\"")
        sb.append(this.resultTimestamp.toString())
        sb.append("\"")
        sb.append(",")
        sb.append("\"materialList0\":{")
        sb.append("\"jobTimestamp\":\"")
        sb.append(materialList0.getJobTimestamp().toString())
        sb.append("\",\"size\":")
        sb.append(materialList0.size())
        sb.append("}")
        sb.append(",")
        sb.append("\"materialList1\":{")
        sb.append("\"jobTimestamp\":\"")
        sb.append(materialList1.getJobTimestamp().toString())
        sb.append("\",\"size\":")
        sb.append(materialList1.size())
        sb.append("}")
        if (fullContent) {
            sb.append(",")
            int count = 0
            sb.append("\"mProductList\":")
            sb.append("[")
            mProductList.each { MProduct da ->
                if (count > 0) sb.append(",")
                sb.append(da.toString())
                count += 1
            }
            sb.append("]")
        }
        sb.append("}")
        return sb.toString()
    }

    static Builder builder(MaterialList left, MaterialList right) {
        return new Builder(left, right)
    }

    /**
     *
     */
    static class Builder {
        // required
        private final List<MProduct> mProductList
        private final MaterialList materialList0
        private final MaterialList materialList1
        private final JobTimestamp resolventTimestamp
        //
        private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT
        private SortKeys sortKeys = SortKeys.NULL_OBJECT
        //
        Builder(MaterialList materialList0, MaterialList materialList1) {
            this.materialList0 = materialList0
            this.materialList1 = materialList1
            this.mProductList = new ArrayList<>()
            int order = materialList0.getJobTimestamp() <=> materialList1.getJobTimestamp()
            if (order <= 0) {
                this.resolventTimestamp =
                        JobTimestamp.laterThan(materialList0.getJobTimestamp(), materialList1.getJobTimestamp())
            } else {
                throw new IllegalArgumentException("left=${materialList0.getJobTimestamp()}, right=${materialList1.getJobTimestamp()}. expected left < right.")
            }
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
        MProductGroup build() {
            return new MProductGroup(this)
        }
    }
}
