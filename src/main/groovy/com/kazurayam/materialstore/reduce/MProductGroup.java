package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MProductGroup implements Iterable<MaterialProduct>, TemplateReady {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroup.class);
    private final List<MaterialProduct> mProductList;
    private final JobTimestamp resultTimestamp;
    private MaterialList materialList0;
    private MaterialList materialList1;
    private IgnoreMetadataKeys ignoreMetadataKeys;
    private IdentifyMetadataValues identifyMetadataValues;
    private final SortKeys sortKeys;
    private boolean readyToReport;

    private MProductGroup(Builder builder) {
        this.materialList0 = builder.materialList0;
        this.materialList1 = builder.materialList1;
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys;
        this.identifyMetadataValues = builder.identifyMetadataValues;
        this.sortKeys = builder.sortKeys;
        this.resultTimestamp = builder.resultTimestamp;
        // this is the most mysterious part of the materialstore library
        this.mProductList = zipMaterials(materialList0, materialList1, this.resultTimestamp, ignoreMetadataKeys, identifyMetadataValues, sortKeys);
        // at this timing the MProducts are not yet filled with the diff information, they are still vacant.
    }

    /**
     * Deep-copy constructor
     *
     */
    public MProductGroup(MProductGroup source) {
        this.materialList0 = new MaterialList(source.materialList0);
        this.materialList1 = new MaterialList(source.materialList1);
        this.ignoreMetadataKeys = source.getIgnoreMetadataKeys();        // IgnoreMetadataKeys is immutable
        this.identifyMetadataValues = source.getIdentifyMetadataValues();// IdentifyMetadataValues is immutable
        this.sortKeys = source.getSortKeys();                            // SortKeys is immutable
        final List<MaterialProduct> tmp = new ArrayList<>();
        for (MaterialProduct sourceMProduct : source) {
            tmp.add(new MaterialProduct(sourceMProduct));
        }
        this.mProductList = tmp;
        this.resultTimestamp = source.getResultTimestamp();
        this.readyToReport = source.isReadyToReport();
    }

    public void add(MaterialProduct mProduct) {
        mProduct.annotate(ignoreMetadataKeys, identifyMetadataValues);
        mProductList.add(mProduct);
    }

    public boolean update(MaterialProduct mProduct) {
        boolean wasPresent = mProductList.remove(mProduct);
        this.add(mProduct);
        return wasPresent;
    }

    public int countWarnings(final Double criteria) {
        Objects.requireNonNull(criteria);
        int count = 0;
        for (MaterialProduct mProduct : mProductList) {
            assert criteria != null;
            assert mProduct != null;
            assert mProduct.getDiffRatio() != null;
            if (criteria < mProduct.getDiffRatio()) {
                count += 1;
            }
        }
        return count;
    }

    public MaterialProduct get(int index) {
        return mProductList.get(index);
    }

    public JobTimestamp getResultTimestamp() {
        return this.resultTimestamp;
    }

    public IdentifyMetadataValues getIdentifyMetadataValues() {
        return this.identifyMetadataValues;
    }

    public JobTimestamp getJobTimestampLeft() {
        return this.getMaterialListLeft().getJobTimestamp();
    }

    public JobTimestamp getJobTimestampRight() {
        return this.getMaterialListRight().getJobTimestamp();
    }

    public JobTimestamp getJobTimestampPrevious() {
        return this.getMaterialListPrevious().getJobTimestamp();
    }

    public JobTimestamp getJobTimestampFollowing() {
        return this.getMaterialListFollowing().getJobTimestamp();
    }

    public JobName getJobName() {
        return this.getMaterialListRight().getJobName();
    }

    public IgnoreMetadataKeys getIgnoreMetadataKeys() {
        return this.ignoreMetadataKeys;
    }

    public MaterialList getMaterialListLeft() {
        return this.materialList0;
    }

    public MaterialList getMaterialListRight() {
        return this.materialList1;
    }

    public MaterialList getMaterialListPrevious() {
        return this.materialList0;
    }

    public MaterialList getMaterialListFollowing() {
        return this.materialList1;
    }

    public SortKeys getSortKeys() {
        return this.sortKeys;
    }

    public boolean isReadyToReport() {
        return this.readyToReport;
    }

    public Iterator<MaterialProduct> iterator() {
        return mProductList.iterator();
    }

    public void setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
        this.identifyMetadataValues = identifyMetadataValues;
    }

    public void setIgnoreMetadataKeys(IgnoreMetadataKeys ignoreMetadataKeys) {
        this.ignoreMetadataKeys = ignoreMetadataKeys;
    }

    public void setMaterialListLeft(MaterialList materialList) {
        this.materialList0 = materialList;
    }

    public void setMaterialListRight(MaterialList materialList) {
        this.materialList1 = materialList;
    }

    /**
     * supposed to be used only by DifferDriverImpl
     */
    public void setReadyToReport(boolean readyToReport) {
        this.readyToReport = readyToReport;
    }

    public int size() {
        return mProductList.size();
    }

    public void sort() {
        Collections.sort(mProductList);
    }

    public List<QueryOnMetadata> getQueryOnMetadataList() {
        final List<QueryOnMetadata> list = new ArrayList<>();
        for (MaterialProduct mProduct : mProductList) {
            QueryOnMetadata query = mProduct.getQueryOnMetadata();
            QueryOnMetadata deepCopy = QueryOnMetadata.builder(query).build();
            list.add(deepCopy);
        }
        return list;
    }

    /**
     *
     */
    public static List<MaterialProduct> zipMaterials(final MaterialList leftList, final MaterialList rightList, final JobTimestamp resultTimestamp, final IgnoreMetadataKeys ignoreMetadataKeys, final IdentifyMetadataValues identifyMetadataValues, final SortKeys sortKeys) {
        Objects.requireNonNull(leftList);
        Objects.requireNonNull(rightList);
        Objects.requireNonNull(resultTimestamp);
        Objects.requireNonNull(ignoreMetadataKeys);
        Objects.requireNonNull(identifyMetadataValues);
        Objects.requireNonNull(sortKeys);

        // the result
        final List<MaterialProduct> mProductList = new ArrayList<>();

        //
        Iterator<Material> rightIter = rightList.iterator();
        while (rightIter.hasNext()) {
            Material right = rightIter.next();
            final FileType rightFileType = right.getIndexEntry().getFileType();
            Metadata rightMetadata = right.getIndexEntry().getMetadata();
            final QueryOnMetadata rightPattern = QueryOnMetadata.builder(rightMetadata, ignoreMetadataKeys).build();
            //
            logger.debug("--------");
            logger.debug("right " + right.getShortId() + " " + rightFileType.getExtension() + " pattern: " + rightPattern);
            int foundLeftCount = 0;

            Iterator<Material> leftIter = leftList.iterator();
            while (leftIter.hasNext()) {
                Material left = leftIter.next();
                final FileType leftFileType = left.getIndexEntry().getFileType();
                final Metadata leftMetadata = left.getIndexEntry().getMetadata();
                if (leftFileType.equals(rightFileType) && (rightPattern.matches(leftMetadata) || identifyMetadataValues.matches(leftMetadata))) {
                    MaterialProduct mp = new MaterialProduct.Builder(left, right, resultTimestamp).setQueryOnMetadata(rightPattern).sortKeys(sortKeys).build();
                    mProductList.add(mp);
                    logger.debug("left Y " + left.getShortId() + " " + leftFileType.getExtension() + " " + leftMetadata);
                    foundLeftCount += 1;
                } else {
                    logger.debug("left N " + left.getShortId() + " " + leftFileType.getExtension() + " " + leftMetadata);
                }
            }
            if (foundLeftCount == 0) {
                MaterialProduct mp =
                        new MaterialProduct.Builder(Material.NULL_OBJECT, right, resultTimestamp)
                                .setQueryOnMetadata(rightPattern)
                                .sortKeys(sortKeys)
                                .build();
                mProductList.add(mp);
            }

            logger.debug("foundLeftCount=" + foundLeftCount);
            if (foundLeftCount == 0 || foundLeftCount >= 2) {
                logger.info("foundLeftCount=" + foundLeftCount + " is unusual");
            }
        }

        //
        Iterator<Material> leftIter2 = leftList.iterator();
        while (leftIter2.hasNext()) {
            Material left = leftIter2.next();
            final FileType leftFileType = left.getIndexEntry().getFileType();
            Metadata leftMetadata = left.getIndexEntry().getMetadata();
            final QueryOnMetadata leftPattern =
                    QueryOnMetadata.builder(leftMetadata, ignoreMetadataKeys).build();
            logger.debug("--------");
            logger.debug("left " + left.getShortId() + " " + leftFileType.toString() + " pattern: " + leftPattern);
            int foundRightCount = 0;
            //
            Iterator<Material> rightIter2 = rightList.iterator();
            while (rightIter2.hasNext()) {
                Material right = rightIter2.next();
                final FileType rightFileType = right.getIndexEntry().getFileType();
                final Metadata rightMetadata = right.getIndexEntry().getMetadata();
                if (rightFileType.equals(leftFileType) &&
                        (leftPattern.matches(rightMetadata) || identifyMetadataValues.matches(rightMetadata))) {
                    // this must have been found matched already; no need to create a MProduct
                    logger.debug("right Y " + right.getShortId() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                    foundRightCount += 1;
                } else {
                    logger.debug("right N " + right.getShortId() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                }
            }
            if (foundRightCount == 0) {
                MaterialProduct mProduct =
                        new MaterialProduct.Builder(left, Material.NULL_OBJECT, resultTimestamp)
                        .setQueryOnMetadata(leftPattern)
                        .sortKeys(sortKeys)
                        .build();
                mProductList.add(mProduct);
            }
            logger.debug("foundRightCount=" + foundRightCount);
            if (foundRightCount == 0 || foundRightCount >= 2) {
                logger.info("foundRightCount=" + foundRightCount + " is unusual");
            }
        }
        Collections.sort(mProductList);
        return mProductList;
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public String toJson() {
        return getDescription(true);
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }

    }

    public String getDescription(boolean fullContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobName\":\"");
        sb.append(this.getJobName().toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"resultTimestamp\":\"");
        sb.append(this.resultTimestamp.toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"isReadyToReport\":");
        sb.append(this.isReadyToReport());
        sb.append(",");
        sb.append("\"ignoreMetadataKeys\":");
        sb.append(this.getIgnoreMetadataKeys().toJson());
        sb.append(",");
        sb.append("\"materialList0\":{");
        sb.append("\"jobTimestamp\":\"");
        sb.append(materialList0.getJobTimestamp().toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"queryOnMetadata\":");
        sb.append(materialList0.getQueryOnMetadata().toJson());
        sb.append(",");
        sb.append("\"size\":");
        sb.append(materialList0.size());
        sb.append("}");
        sb.append(",");
        sb.append("\"materialList1\":{");
        sb.append("\"jobTimestamp\":\"");
        sb.append(materialList1.getJobTimestamp().toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"queryOnMetadata\":");
        sb.append(materialList1.getQueryOnMetadata().toJson());
        sb.append(",");
        sb.append("\"size\":");
        sb.append(materialList1.size());
        sb.append("}");
        if (fullContent) {
            sb.append(",");
            int count = 0;
            sb.append("\"mProductList\":");
            sb.append("[");
            for (MaterialProduct mProduct : mProductList) {
                if (count > 0) {
                    sb.append(",");
                }
                sb.append(mProduct.toJson());
                count += 1;
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    public String getDescription() {
        return getDescription(false);
    }

    public static Builder builder(MaterialList left, MaterialList right) {
        return new Builder(left, right);
    }


    /**
     *
     */
    public static class Builder {

        private final MaterialList materialList0;
        private final MaterialList materialList1;
        private final JobTimestamp resultTimestamp;
        private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT;
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT;
        private SortKeys sortKeys = SortKeys.NULL_OBJECT;

        public Builder(final MaterialList materialList0, final MaterialList materialList1) {
            this.materialList0 = materialList0;
            this.materialList1 = materialList1;
            int order = materialList0.getJobTimestamp().compareTo(materialList1.getJobTimestamp());
            if (order <= 0) {
                this.resultTimestamp = JobTimestamp.laterThan(materialList0.getJobTimestamp(), materialList1.getJobTimestamp());
            } else {
                throw new IllegalArgumentException("left=" + materialList0.getJobTimestamp() + ", right=" +
                        materialList1.getJobTimestamp() + ". expected left < right.");
            }
        }

        public Builder ignoreKeys(String... keys) {
            IgnoreMetadataKeys imk = new IgnoreMetadataKeys.Builder().ignoreKeys(keys).build();
            return setIgnoreMetadataKeys(imk);
        }

        public Builder setIgnoreMetadataKeys(IgnoreMetadataKeys ignoreMetadataKeys) {
            this.ignoreMetadataKeys = ignoreMetadataKeys;
            return this;
        }

        public Builder identifyWithRegex(Map<String, String> pairs) {
            IdentifyMetadataValues imv = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(pairs).build();
            return setIdentifyMetadataValues(imv);
        }

        public Builder setIdentifyMetadataValues(IdentifyMetadataValues identifyMetadataValues) {
            this.identifyMetadataValues = identifyMetadataValues;
            return this;
        }

        public Builder sort(String... args) {
            SortKeys sortKeys = new SortKeys(args);
            return this.setSortKeys(sortKeys);
        }

        public Builder setSortKeys(SortKeys sortKeys) {
            this.sortKeys = sortKeys;
            return this;
        }

        public MProductGroup build() {
            return new MProductGroup(this);
        }
    }

}
