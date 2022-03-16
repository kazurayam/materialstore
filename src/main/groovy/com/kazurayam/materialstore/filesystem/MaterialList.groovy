package com.kazurayam.materialstore.filesystem

import com.google.gson.Gson
import com.kazurayam.materialstore.util.GsonHelper
import com.kazurayam.materialstore.util.JsonUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class MaterialList implements Jsonifiable, TemplateReady {

    private static Logger logger = LoggerFactory.getLogger(MaterialList.class.getName())

    public static final NULL_OBJECT =
            new MaterialList(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT,
                    QueryOnMetadata.NULL_OBJECT)

    private JobName jobName

    private JobTimestamp jobTimestamp

    private QueryOnMetadata query

    private List<Material> materialList

    MaterialList(JobName jobName, JobTimestamp jobTimestamp,
                 QueryOnMetadata query) {
        Objects.requireNonNull(jobName)
        Objects.requireNonNull(jobTimestamp)
        Objects.requireNonNull(query)
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        this.query = query
        //
        this.materialList = new ArrayList<Material>()
    }

    /**
     * Deep-copy constructor
     * @param source
     */
    MaterialList(MaterialList source) {
        this.jobName = source.jobName
        this.jobTimestamp = source.jobTimestamp         // JobTimestamp is immutable
        this.query = source.queryOnMetadata   // QueryOnMetadata is immutable
        this.materialList = new ArrayList<>(source.materialList)  // Material is immutable
    }

    //--------------- List-like methods -----------

    void add(Material material) {
        materialList.add(material)
    }

    void add(List<Material> list) {
        list.each {material ->
            materialList.add(material)
        }
    }

    Material get(int index) {
        return materialList.get(index)
    }

    boolean contains(Material material) {
        return materialList.contains(material)
    }

    boolean containsMaterialsSimilarTo(Material baseMaterial) {
        List<Material> found = findMaterialsSimilarTo(baseMaterial)
        return (found.size() > 0)
    }

    List<Material> findMaterialsSimilarTo(Material baseMaterial) {
        List<Material> list = new ArrayList<>()
        for (Material targetMaterial : materialList) {
            boolean similar = targetMaterial.isSimilar(baseMaterial)
            if (similar) {
                logger.debug(String.format(
                        "[findMaterialsSimilarTo] target=%s is similar to base=%s",
                        targetMaterial.getShortId(), baseMaterial.getShortId()))

                list.add(targetMaterial)

            } else {
                logger.debug(String.format(
                        "[findMaterialsSimilarTo] target=%s is NOT similar to base=%s",
                        targetMaterial.getShortId(), baseMaterial.getShortId()))
            }
        }
        logger.debug(String.format("[findMaterialsSimilarTo] list.size()=%d", list.size()))
        return list
    }

    int countMaterialsWithIdStartingWith(String idStarter) {
        int count = 0
        for (Material material : materialList) {
            if (material.getIndexEntry().getID().getSha1().startsWith(idStarter)) {
                count += 1
            }
        }
        return count
    }

    int size() {
        return materialList.size()
    }

    Iterator<Material> iterator() {
        return materialList.iterator()
    }

    // --------------- unique method ------------
    JobName getJobName() {
        return this.jobName
    }

    JobTimestamp getJobTimestamp() {
        return this.jobTimestamp
    }

    QueryOnMetadata getQueryOnMetadata() {
        return this.query
    }

    //---------------- java.lang.Object -----------
    @Override
    String toString() {
        return toJson()
    }

    //-------Jsonifiable-----------------------------------------------
    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"jobName\":\"" + jobName.toString() + "\"")
        sb.append(",")
        sb.append("\"jobTimestamp\":\"" + jobTimestamp.toString() + "\"")
        sb.append(",")
        sb.append('''"queryOnMetadata":''')
        sb.append(this.query.toString())
        sb.append(",")
        sb.append('''"materialList":''')
        int count = 0
        sb.append("[")
        this.materialList.each {material ->
            if (count > 0) {
                sb.append(",")
            }
            sb.append(material.toString())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson(), Map.class)
        } else {
            return toJson()
        }
    }

    //--------TemplateReady--------------------------------------------
    @Override
    Map<String, Object> toTemplateModel() {
        // convert JSON string to Java Map
        Map<String, Object> map = new Gson().fromJson(toJson(), Map.class)
        return map
    }

    @Override
    String toTemplateModelAsJson() {
        return toTemplateModelAsJson(false)
    }

    @Override
    String toTemplateModelAsJson(boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint)
        Map<String, Object> model = toTemplateModel()
        return gson.toJson(model)
    }
}
