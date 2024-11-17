package com.kazurayam.materialstore.base.reduce;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kazurayam.materialstore.core.DiffColor;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProductComparator;
import com.kazurayam.materialstore.base.reduce.zipper.Zipper;
import com.kazurayam.materialstore.core.ID;
import com.kazurayam.materialstore.core.Identifiable;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialIO;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.SortKeys;
import com.kazurayam.materialstore.core.TemplateReadySortable;
import com.kazurayam.materialstore.core.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.core.metadata.IgnoreMetadataKeys;
import com.kazurayam.materialstore.util.GsonHelper;
import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A group of MaterialProduct objects.
 *
 */
public final class MaterialProductGroup
        implements Iterable<MaterialProduct>, TemplateReadySortable, Identifiable {

    public static final MaterialProductGroup NULL_OBJECT =
            new Builder(
                    MaterialList.NULL_OBJECT,
                    MaterialList.NULL_OBJECT).build();

    private static final Logger logger = LoggerFactory.getLogger(MaterialProductGroup.class);
    private List<MaterialProduct> materialProductList;
    private final JobTimestamp resultTimestamp;
    private MaterialList materialList0;
    private MaterialList materialList1;
    private IgnoreMetadataKeys ignoreMetadataKeys;
    private IdentifyMetadataValues identifyMetadataValues;
    private final SortKeys sortKeys;
    private Double threshold;
    private boolean readyToReport;
    private String labelLeft;
    private String labelRight;

    private MaterialProductGroup(Builder builder) {
        this.materialList0 = builder.materialList0;
        this.materialList1 = builder.materialList1;
        this.ignoreMetadataKeys = builder.ignoreMetadataKeys;
        this.identifyMetadataValues = builder.identifyMetadataValues;
        this.sortKeys = builder.sortKeys;
        this.threshold = builder.threshold;
        this.resultTimestamp = builder.resultTimestamp;
        this.materialProductList = builder.materialProductList;
        this.labelLeft = builder.labelLeft;
        this.labelRight = builder.labelRight;
    }

    /*
     * Deep-copy constructor
     */
    public MaterialProductGroup(MaterialProductGroup source) {
        Objects.requireNonNull(source);
        this.materialList0 = new MaterialList(source.materialList0);
        this.materialList1 = new MaterialList(source.materialList1);
        this.ignoreMetadataKeys = source.getIgnoreMetadataKeys();        // IgnoreMetadataKeys is immutable
        this.identifyMetadataValues = source.getIdentifyMetadataValues();// IdentifyMetadataValues is immutable
        this.sortKeys = source.getSortKeys();                            // SortKeys is immutable
        this.threshold = source.getThreshold();
        this.resultTimestamp = source.getJobTimestampOfReduceResult();
        this.readyToReport = source.isReadyToReport();
        this.labelLeft = source.getLabelLeft();
        this.labelRight = source.getLabelRight();
        //
        final List<MaterialProduct> tmp = new ArrayList<>();
        for (MaterialProduct sourceMProduct : source) {
            MaterialProduct mp = MaterialProduct.clone(sourceMProduct);
            mp.annotate(this.ignoreMetadataKeys, this.identifyMetadataValues);
            tmp.add(mp);
        }
        this.materialProductList = tmp;
    }

    /*
     * Deep copy constructor, plus replace the internal list of MaterialProduct with
     * given instance.
     * @param source
     * @param newMaterialProductList
     */
    public MaterialProductGroup(MaterialProductGroup source,
                                List<MaterialProduct> newMaterialProductList) {
        this(source);
        Objects.requireNonNull(newMaterialProductList);
        this.materialProductList = newMaterialProductList;
    }

    /*
     * convenience method for unit-testing
     */
    public void add(MaterialProduct mProduct) {
        mProduct.annotate(ignoreMetadataKeys, identifyMetadataValues);
        materialProductList.add(mProduct);
    }


    public int countWarnings(final Double threshold) {
        return this.countExceeding(threshold);
    }

    /*
     * count the number of MaterialProduct objects that have diffRatio
     * greater than the threshold given.
     *
     * @param threshold
     * @return
     */
    public int countExceeding(final Double threshold) {
        Objects.requireNonNull(threshold);
        int count = 0;
        for (MaterialProduct mProduct : materialProductList) {
            assert mProduct != null;
            assert mProduct.getDiffRatio() != null;
            if (mProduct.getDiffRatio() > threshold) {
                count += 1;
            }
        }
        return count;
    }

    public MaterialProduct get(int index) {
        return materialProductList.get(index);
    }


    @Override
    public ID getID() {
        String json = this.toJson();
        return new ID(MaterialIO.hashJDK(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String getShortID() {
        String id = this.getID().toString();
        return id.substring(0, 7);
    }


    public JobTimestamp getJobTimestampOfReduceResult() {
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

    public Double getThreshold() { return this.threshold; }

    public int getCountWarning() {
        List<MaterialProduct> filtered = new ArrayList<>();
        for (MaterialProduct mp : this) {
            if (mp.getDiffRatio() > this.getThreshold()) {
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

    public String getLabelLeft() { return this.labelLeft; }
    public String getLabelRight() { return this.labelRight; }

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

    public void setThreshold(Double threshold) {
        Objects.requireNonNull(threshold);
        this.threshold = threshold;
    }

    /*
     * supposed to be used only by DifferDriverImpl
     */
    public void setReadyToReport(boolean readyToReport) {
        this.readyToReport = readyToReport;
    }

    public int size() {
        return materialProductList.size();
    }

    public void order(SortKeys sortKeys) {
        MaterialProductComparator comparator = new MaterialProductComparator(sortKeys);
        materialProductList.sort(comparator);
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
        return toVariableJson(new SortKeys(), true);
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }

    }

    public String toSummary() {
        return JsonUtil.prettyPrint(
                this.toVariableJson(new SortKeys(),
                        false));
    }

    String toVariableJson(SortKeys sortKeys,
                          boolean withMaterialProductList) {
        //TODO sortKeys is not yet used
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
        sb.append("\"threshold\":");
        sb.append(this.getThreshold());
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
        if (withMaterialProductList) {
            sb.append(",");
            int count = 0;
            sb.append("\"materialProductList\":");
            sb.append("[");
            for (MaterialProduct mProduct : materialProductList) {
                if (count > 0) {
                    sb.append(",");
                }
                sb.append(
                        mProduct.toVariableJson(
                                sortKeys
                        ));
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
        sb.append(",");
        sb.append("\"labelLeft\":\"");
        sb.append(getLabelLeft());
        sb.append("\"");
        sb.append(",");
        sb.append("\"labelRight\":\"");
        sb.append(getLabelRight());
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Map<String, Object> toTemplateModel(SortKeys sortKeys) {
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        return new Gson().fromJson(
                toVariableJson(sortKeys, true),
                mapType);
    }

    @Override
    public String toTemplateModelAsJson(SortKeys sortKeys, boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint);
        Map<String, Object> model = toTemplateModel(sortKeys);
        return gson.toJson(model);
    }

    public static Builder builder(MaterialList left, MaterialList right) {
        return new Builder(left, right);
    }


    /**
     *
     */
    public static class Builder {

        private Logger logger = LoggerFactory.getLogger(Builder.class);

        private final MaterialList materialList0;
        private final MaterialList materialList1;
        private final JobTimestamp resultTimestamp;
        private IgnoreMetadataKeys ignoreMetadataKeys = IgnoreMetadataKeys.NULL_OBJECT;
        private IdentifyMetadataValues identifyMetadataValues = IdentifyMetadataValues.NULL_OBJECT;
        private SortKeys sortKeys = SortKeys.NULL_OBJECT;
        private final Double threshold;
        private List<MaterialProduct> materialProductList;
        private String labelLeft;
        private String labelRight;
        private DiffColor withDiffColor;

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
            this.threshold = 0.0d;
            this.labelLeft = materialList0.getJobTimestamp().toString();
            this.labelRight = materialList1.getJobTimestamp().toString();
            this.withDiffColor = DiffColor.DEFAULT;
        }

        public Builder ignoreKey(String key) {
            ignoreMetadataKeys.add(key);
            return this;
        }

        public Builder ignoreKeys(String... keys) {
            this.ignoreKeys(Arrays.asList(keys));
            return this;
        }

        public Builder ignoreKeys(List<String> keys) {
            this.ignoreMetadataKeys.addAll(keys);
            return this;
        }

        public Builder ignoreKeys(IgnoreMetadataKeys imk) {
            this.ignoreMetadataKeys.addAll(imk);
            return this;
        }

        public Builder setIgnoreMetadataKeys(IgnoreMetadataKeys imk) {
            this.ignoreMetadataKeys = imk;
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

        public Builder labelLeft(String labelLeft) {
            this.labelLeft = labelLeft;
            return this;
        }

        public Builder labelRight(String labelRight) {
            this.labelRight = labelRight;
            return this;
        }

        public Builder withDiffColor(DiffColor withDiffColor) {
            this.withDiffColor = withDiffColor;
            return this;
        }

        public MaterialProductGroup build() {

            /* =============================================================
             * this is the most mysterious part of the materialstore library
             */
            Zipper zipper =
                    new Zipper(ignoreMetadataKeys, identifyMetadataValues)
                            .withDiffColor(withDiffColor);
            this.materialProductList =
                    zipper.zipMaterials(materialList0, materialList1, resultTimestamp);

            /* ============================================================
             * at this timing the materialProductList variable is null.
             * the instance han no diff information yet
             */

            return new MaterialProductGroup(this);
        }
    }
}
