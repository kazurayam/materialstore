package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.filesystem.Identifiable;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialIO;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.TemplateReady;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.reduce.zipper.Zipper;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A group of MaterialProduct objects.
 *
 */
public final class MProductGroup
        implements Iterable<MaterialProduct>, TemplateReady, Identifiable {

    private static final Logger logger = LoggerFactory.getLogger(MProductGroup.class);
    private final List<MaterialProduct> materialProductList;
    private final JobTimestamp resultTimestamp;
    private MaterialList materialList0;
    private MaterialList materialList1;
    private IgnoreMetadataKeys ignoreMetadataKeys;
    private IdentifyMetadataValues identifyMetadataValues;
    private final SortKeys sortKeys;
    private Double criteria;
    private boolean readyToReport;

    private MProductGroup(Builder builder) {
        this.materialList0 = builder.materialList0;
        this.materialList1 = builder.materialList1;
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys;
        this.identifyMetadataValues = builder.identifyMetadataValues;
        this.sortKeys = builder.sortKeys;
        this.criteria = builder.criteria;
        this.resultTimestamp = builder.resultTimestamp;

        // this is the most mysterious part of the materialstore library
        Zipper zipper = new Zipper(ignoreMetadataKeys, identifyMetadataValues, sortKeys);
        this.materialProductList = zipper.zipMaterials(materialList0, materialList1, resultTimestamp);

        // at this timing the MProducts are not yet filled with the diff information.
        // the diff information is still vacant.
        // The following code processes the MProductGroup object and fill with the diff information:

        // Inspector inspector = Inspector.newInstance(store)
        // MProductGroup processed = inspector.process(reduced)
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
        this.criteria = source.getCriteria();
        final List<MaterialProduct> tmp = new ArrayList<>();
        for (MaterialProduct sourceMProduct : source) {
            tmp.add(new MaterialProduct(sourceMProduct));
        }
        this.materialProductList = tmp;
        this.resultTimestamp = source.getResultTimestamp();
        this.readyToReport = source.isReadyToReport();
    }

    public void add(MaterialProduct mProduct) {
        mProduct.annotate(ignoreMetadataKeys, identifyMetadataValues);
        materialProductList.add(mProduct);
    }

    public boolean update(MaterialProduct mProduct) {
        boolean wasPresent = materialProductList.remove(mProduct);
        this.add(mProduct);
        return wasPresent;
    }

    public int countWarnings(final Double criteria) {
        return this.countExceeding(criteria);
    }

    /**
     * count the number of MaterialProduct objects that have diffRatio
     * greater than the criteria given.
     *
     * @param criteria
     * @return
     */
    public int countExceeding(final Double criteria) {
        Objects.requireNonNull(criteria);
        int count = 0;
        for (MaterialProduct mProduct : materialProductList) {
            assert mProduct != null;
            assert mProduct.getDiffRatio() != null;
            if (mProduct.getDiffRatio() > criteria) {
                count += 1;
            }
        }
        return count;
    }

    public MaterialProduct get(int index) {
        return materialProductList.get(index);
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

    public Double getCriteria() { return this.criteria; }

    public int getCountWarning() {
        List<MaterialProduct> filtered = new ArrayList<>();
        for (MaterialProduct mp : this) {
            if (mp.getDiffRatio() > this.getCriteria()) {
                filtered.add(mp);
            }
        }
        return filtered.size();
    }

    public int getNumberOfBachelors() {
        List<MaterialProduct> filtered =
                this.materialProductList.stream()
                        .filter(MaterialProduct::isBachelor)
                        .collect(Collectors.toList());
        return filtered.size();
    }

    public long getCountTotal() { return this.materialProductList.size(); }

    public boolean isReadyToReport() {
        return this.readyToReport;
    }

    public Iterator<MaterialProduct> iterator() {
        return materialProductList.iterator();
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

    public void setCriteria(Double criteria) {
        Objects.requireNonNull(criteria);
        this.criteria = criteria;
    }

    /**
     * supposed to be used only by DifferDriverImpl
     */
    public void setReadyToReport(boolean readyToReport) {
        this.readyToReport = readyToReport;
    }

    public int size() {
        return materialProductList.size();
    }

    public void sort() {
        Collections.sort(materialProductList);
    }

    public List<QueryOnMetadata> getQueryOnMetadataList() {
        final List<QueryOnMetadata> list = new ArrayList<>();
        for (MaterialProduct mProduct : materialProductList) {
            QueryOnMetadata query = mProduct.getQueryOnMetadata();
            QueryOnMetadata deepCopy = QueryOnMetadata.builder(query).build();
            list.add(deepCopy);
        }
        return list;
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

    @Override
    public String getId() {
        String json = this.toJson();
        return MaterialIO.hashJDK(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getShortId() {
        String id = this.getId();
        return id.substring(0, 7);
    }

    public String getDescription(boolean fullContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobName\":\"");
        sb.append(this.getJobName().toString());
        sb.append("\"");
        sb.append(",");
        //sb.append("\"resultTimestamp\":\"");
        //sb.append(this.resultTimestamp.toString());
        //sb.append("\"");
        //sb.append(",");
        sb.append("\"criteria\":");
        sb.append(this.getCriteria());
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
            sb.append("\"materialProductList\":");
            sb.append("[");
            for (MaterialProduct mProduct : materialProductList) {
                if (count > 0) {
                    sb.append(",");
                }
                sb.append(mProduct.toJson());
                count += 1;
            }
            sb.append("]");
        }
        sb.append(",");
        sb.append("\"countWarning\":");
        sb.append(getCountWarning());
        sb.append(",");
        sb.append("\"countIgnorable\":");
        sb.append(0);
        sb.append(",");
        sb.append("\"countTotal\":");
        sb.append(getCountTotal());

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
        private final Double criteria;

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
            this.criteria = 0.0d;
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
