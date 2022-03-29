package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MaterialList implements Jsonifiable, TemplateReady {

    private static final Logger logger = LoggerFactory.getLogger(MaterialList.class.getName());

    public static final MaterialList NULL_OBJECT = new MaterialList(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, QueryOnMetadata.NULL_OBJECT);

    private final JobName jobName;
    private final JobTimestamp jobTimestamp;
    private final QueryOnMetadata query;
    private final List<Material> materialList;

    public MaterialList(JobName jobName, JobTimestamp jobTimestamp, QueryOnMetadata query) {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        Objects.requireNonNull(query);
        this.jobName = jobName;
        this.jobTimestamp = jobTimestamp;
        this.query = query;
        this.materialList = new ArrayList<>();
    }

    /**
     * Deep-copy constructor
     *
     */
    public MaterialList(MaterialList source) {
        this.jobName = source.getJobName();
        this.jobTimestamp = source.getJobTimestamp();// JobTimestamp is immutable
        this.query = source.getQueryOnMetadata();// QueryOnMetadata is immutable
        this.materialList = new ArrayList<>(source.materialList);// Material is immutable
    }

    public void add(Material material) {
        material.getMetadata().annotate(query);
        materialList.add(material);
    }

    public void add(List<Material> list) {
        for (Material material : list) {
            add(material);
        }
    }

    public Material get(int index) {
        return materialList.get(index);
    }

    public boolean contains(Material material) {
        return materialList.contains(material);
    }

    public boolean containsMaterialsSimilarTo(Material baseMaterial) {
        List<Material> found = findMaterialsSimilarTo(baseMaterial);
        return (found.size() > 0);
    }

    public List<Material> findMaterialsSimilarTo(Material baseMaterial) {
        List<Material> list = new ArrayList<>();
        for (Material targetMaterial : materialList) {
            boolean similar = targetMaterial.isSimilar(baseMaterial);
            if (similar) {
                logger.debug(String.format("[findMaterialsSimilarTo] target=%s is similar to base=%s", targetMaterial.getShortId(), baseMaterial.getShortId()));

                list.add(targetMaterial);

            } else {
                logger.debug(String.format("[findMaterialsSimilarTo] target=%s is NOT similar to base=%s", targetMaterial.getShortId(), baseMaterial.getShortId()));
            }

        }

        logger.debug(String.format("[findMaterialsSimilarTo] list.size()=%d", list.size()));
        return list;
    }

    public int countMaterialsWithIdStartingWith(String idStarter) {
        int count = 0;
        for (Material material : materialList) {
            if (material.getIndexEntry().getID().getSha1().startsWith(idStarter)) {
                count += 1;
            }

        }

        return count;
    }

    public int size() {
        return materialList.size();
    }

    public Iterator<Material> iterator() {
        return materialList.iterator();
    }

    public JobName getJobName() {
        return this.jobName;
    }

    public JobTimestamp getJobTimestamp() {
        return this.jobTimestamp;
    }

    public QueryOnMetadata getQueryOnMetadata() {
        return this.query;
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public String toJson() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobName\":\"");
        sb.append(jobName.toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"jobTimestamp\":\"");
        sb.append(jobTimestamp.toString());
        sb.append("\"");
        sb.append(",");
        sb.append("\"queryOnMetadata\":");
        sb.append(this.query.toString());
        sb.append(",");
        sb.append("\"materialList\":");
        int count = 0;
        sb.append("[");
        for (Material material : materialList) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(material.toJson());
            count += 1;
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson(), Map.class);
        } else {
            return toJson();
        }

    }
}
