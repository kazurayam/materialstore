package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.util.JsonUtil;
import com.kazurayam.materialstore.util.DotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MaterialList implements Iterable<Material>, Jsonifiable, TemplateReady,
        Identifiable, GraphvizReady {

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

    @Override
    public String getDotId() {
        return "ML" + this.getShortId();
    }

    public boolean contains(Material material) {
        return materialList.contains(material);
    }

    public boolean containsMaterialsSimilarTo(Material baseMaterial) {
        List<Material> found = findMaterialsSimilarTo(baseMaterial);
        return (found.size() > 0);
    }

    public List<Material> findMaterialsSimilarTo(Material baseMaterial) {
        List<Material> similarMaterials = new ArrayList<>();
        for (Material targetMaterial : materialList) {
            if (targetMaterial.isSimilarTo(baseMaterial)) {
                similarMaterials.add(targetMaterial);
            }
        }
        logger.debug(String.format("[findMaterialsSimilarTo] baseMaterial=%s, similarMaterials.size()=%d",
                baseMaterial.getDescription(), similarMaterials.size()));
        return similarMaterials;
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

    @Override
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

    @Override
    public String toDot() {
        return this.toDot(true);
    }

    /**
     * <PRE>
     *   subgraph cluster_0 {
     *       color=lightgrey;
     *       node [style=filled,color=white];
     *       label="process #2";
     *       color=blue;
     *   M_1865ddd [label="css|{\"URL.host\":\"cdn.jsdelivr.net\", \"URL.path\":\"/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css\", \"URL.port\":\"80\", \"URL.protocol\":\"https\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_4649877 [label="woff2|{\"URL.host\":\"cdn.jsdelivr.net\", \"URL.path\":\"/npm/bootstrap-icons@1.5.0/font/fonts/bootstrap-icons.woff2\", \"URL.port\":\"80\", \"URL.protocol\":\"https\", \"URL.query\":\"856008caa5eb66df68595e734e59580d\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_396f51b [label="css|{\"URL.host\":\"cdn.jsdelivr.net\", \"URL.path\":\"/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css\", \"URL.port\":\"80\", \"URL.protocol\":\"https\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_f522103 [label="js|{\"URL.host\":\"cdn.jsdelivr.net\", \"URL.path\":\"/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js\", \"URL.port\":\"80\", \"URL.protocol\":\"https\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_5a9dcfb [label="js|{\"URL.host\":\"cdnjs.cloudflare.com\", \"URL.path\":\"/ajax/libs/jquery/1.12.4/jquery.min.js\", \"URL.port\":\"80\", \"URL.protocol\":\"https\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_3a27333 [label="html|{\"URL.host\":\"devadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_62bb8a3 [label="png|{\"URL.host\":\"devadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_d0ecc09 [label="jpg|{\"URL.host\":\"devadmin.kazurayam.com\", \"URL.path\":\"/umineko-1960x1960.jpg\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_DevelopmentEnv\"}"];
     *   M_1865ddd -> M_4649877 [style=invis];
     *   M_4649877 -> M_396f51b [style=invis];
     *   M_396f51b -> M_f522103 [style=invis];
     *   M_f522103 -> M_5a9dcfb [style=invis];
     *   M_5a9dcfb -> M_3a27333 [style=invis];
     *   M_3a27333 -> M_62bb8a3 [style=invis];
     *   M_62bb8a3 -> M_d0ecc09 [style=invis];
     *   }
     * </PRE>
     */
    @Override
    public String toDot(boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("subgraph cluster_0 {");
        pw.println("  color=lightgrey;");
        pw.println("  node [style=filled];");
        pw.println("  label=\"" + this.getJobName()
                + "/" + this.getJobTimestamp() + "/\"");
        pw.println("  color=blue;");
        for (Material material : this) {
            pw.println("  " + material.toDot(false));
        }
        String prevId = this.get(0).getDotId();
        for (int i = 1; i < this.size(); i++) {
            String currId = this.get(i).getDotId();
            pw.println("  " + prevId + " -> " + currId + " [style=invis];");
            prevId = currId;
        }
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return DotUtil.standalone(sw.toString());
        } else {
            return sw.toString();
        }
    }
}
