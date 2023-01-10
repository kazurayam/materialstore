package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.util.StringUtils;
import com.kazurayam.subprocessj.Subprocess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * generates a DOT script with which you can draw diagrams
 * of the objects using GraphViz
 */
public class DotGenerator {

    public static final String INDENT = "    ";

    //-----------------------------------------------------------------
    /*
     * generate DOT of a Material object
     */
    public static String generateDot(Material material,
                                     boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("subgraph cluster_M" + 0 + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"" + material.getJobName()
                + "/" + material.getJobTimestamp() + "/\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "]");
        // node
        GraphNodeId nodeId = GraphNodeIdResolver.resolveIdOfMaterialSolo(material);
        pw.println(new MaterialAsGraphNode(nodeId, material).toGraphNode());
        // no edge
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraph(sw.toString(), "Material");
        } else {
            return sw.toString();
        }
    }

    //-----------------------------------------------------------------
    /*
     * generate DOT of a MaterialList object
     */
    public static String generateDot(MaterialList materialList,
                                     Map<String, String> options, boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_ML" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"" + materialList.getJobName()
                + "/" + materialList.getJobTimestamp() + "/\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
        // nodes
        for (Material material : materialList) {
            GraphNodeId nodeId =
                    GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(materialList, material);
            String nodeStmt = new MaterialAsGraphNode(nodeId, material).toGraphNode();
            pw.println(INDENT + nodeStmt);
        }
        // edges
        GraphNodeId precedingNodeId =
                GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(materialList, materialList.get(0));
        for (int i = 1; i < materialList.size(); i++) {
            GraphNodeId currentNodeId =
                    GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(materialList, materialList.get(i));
            pw.println(INDENT + precedingNodeId + ":f0" + " -> " + currentNodeId + ":f0" +  " [style=invis, weight=10];");
            precedingNodeId = currentNodeId;
        }
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraph(sw.toString(), "MaterialList");
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MaterialList materialList) {
        return generateDot(materialList, Collections.emptyMap(), true);
    }

    //-----------------------------------------------------------------
    /*
     *
     */
    public static String generateDot(MaterialProduct materialProduct,
                                     Map<String, String> options, boolean standalone) throws MaterialstoreException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MP" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"left: "
                + materialProduct.getLeft().getJobTimestamp()
                + " | diff: "
                + materialProduct.getDiff().getJobTimestamp()
                + " | right: "
                + materialProduct.getRight().getJobTimestamp()
                + "\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
        GraphNodeId leftNodeId =
                GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(materialProduct, Role.L);
        GraphNodeId copyNodeId =
                GraphNodeIdResolver.resolveIdOfQueryInMaterialProduct(materialProduct);
        GraphNodeId rightNodeId =
                GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(materialProduct, Role.R);
        GraphNodeId diffNodeId =
                GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(materialProduct, Role.D);
        // left Material
        String leftNodeStatement =
                new MaterialAsGraphNode(leftNodeId, materialProduct.getLeft()).toGraphNode();
        pw.println(INDENT + leftNodeStatement);
        // query that connects the left and the right
        GraphNodeId queryNodeId =
                GraphNodeIdResolver.resolveIdOfQueryInMaterialProduct(materialProduct);
        QueryAsGraphNode queryNode =
                new QueryAsGraphNode(queryNodeId, materialProduct.getQueryOnMetadata());
        String queryNodeStatement = queryNode.toGraphNode();
        pw.println(INDENT + queryNodeStatement);
        // right Material
        String rightGraphNodeStatement =
                new MaterialAsGraphNode(rightNodeId, materialProduct.getRight()).toGraphNode();
        pw.println(INDENT + rightGraphNodeStatement);
        // diff Material
        String diffGraphNodeStatement =
                new MaterialAsGraphNode(diffNodeId, materialProduct.getDiff()).toGraphNode();
        pw.println(INDENT + diffGraphNodeStatement);

        // horizontal edges between the left node, the copy node & the right node
        pw.println(INDENT + leftNodeId + ":f2" + " -> "
                + queryNodeId + ":q0" + " [arrowhead=none, weight=1];");
        pw.println(INDENT + queryNodeId + ":q0" + " -> "
                + rightNodeId + ":f0" + " [arrowhead=none, weight=1];");
        // put left + query + right as rank=same
        pw.println(INDENT + "{ rank=same; "
                + leftNodeId + ", "
                + queryNodeId + ", "
                + rightNodeId + "; }");

        // vertical edge between the copy node & the diff node
        pw.println(INDENT + queryNodeId + ":q0" + " -> "
                + diffNodeId + ":d0" + " [arrowhead=none, weight=100];");

        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraph(sw.toString(), "MaterialProduct");
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MaterialProduct materialProduct) throws MaterialstoreException {
        return generateDot(materialProduct, Collections.emptyMap(), true);
    }



    //-----------------------------------------------------------------
    public static String generateDotOfMPGBeforeZip(MaterialProductGroup mProductGroup,
                                                   Map<String, String> options,
                                                   boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MPGBZ" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"" + mProductGroup.getJobName() + "/\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
        pw.println(INDENT + "LEFT -> RIGHT [style=invis];");
        pw.println(INDENT + "{ rank=same; LEFT, RIGHT; }");
        //
        MaterialList leftML = mProductGroup.getMaterialListLeft();
        Material leftTop = leftML.get(0);
        GraphNodeId leftTopId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(leftML, leftTop);
        pw.println(INDENT + "LEFT -> " + leftTopId.getValue() + " [style=invis];");
        //
        MaterialList rightML = mProductGroup.getMaterialListRight();
        Material rightTop = rightML.get(0);
        GraphNodeId rightTopId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialList(rightML, rightTop);
        pw.println(INDENT + "RIGHT -> " + rightTopId.getValue() + " [style=invis];");

        // left Material
        String dotLeft =
                generateDot(mProductGroup.getMaterialListLeft(),
                        Collections.singletonMap("sequenceNumber", "0"), false);
        pw.println(INDENT + dotLeft);
        // right Material
        String dotRight =
                generateDot(mProductGroup.getMaterialListRight(),
                        Collections.singletonMap("sequenceNumber", "1"), false);
        pw.println(INDENT + dotRight);
        // horizontal edge
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraph(sw.toString(), "MProductGroup before zip");
        } else {
            return sw.toString();
        }
    }

    public static String generateDotOfMPGBeforeZip(MaterialProductGroup mProductGroup) {
        return generateDotOfMPGBeforeZip(mProductGroup, Collections.emptyMap(), true);
    }


    //-----------------------------------------------------------------
    /*
     *
     */
    public static String generateDot(MaterialProductGroup mProductGroup,
                                     Map<String, String> options, boolean standalone)
            throws MaterialstoreException {
        // generate a list of "subgraph" statements for MaterialProduct objects
        List<MProductSubgraph> mProductSubgraphList = new ArrayList<>();
        for (MaterialProduct mp : mProductGroup) {
            MProductSubgraph nodeIdPair = new MProductSubgraph(mp);
            mProductSubgraphList.add(nodeIdPair);
        }
        //
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MPG" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"" + mProductGroup.getJobName() + "/\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
        // nodes
        int index = 0;
        for (MProductSubgraph nodeIdPair: mProductSubgraphList) {
            MaterialProduct mp = nodeIdPair.getMaterialProduct();
            Map<String, String> opt = Collections.singletonMap("sequenceNumber", String.valueOf(index));
            pw.println(StringUtils.indentLines(generateDot(mp, opt, false)));
            index += 1;
        }
        // edges
        GraphNodeId previousDiffNodeId = mProductSubgraphList.get(0).getDiffId();
        for (int i = 1; i < mProductSubgraphList.size(); i++) {
            GraphNodeId currentQueryNodeId = mProductSubgraphList.get(i).getQueryId();
            pw.println(INDENT + previousDiffNodeId + ":d0" + " -> "
                    + currentQueryNodeId + ":q0" + " [style=invis,weight=100];");
            previousDiffNodeId = mProductSubgraphList.get(i).getDiffId();
        }
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraph(sw.toString(), "MProductGroup after zip");
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MaterialProductGroup mProductGroup) throws MaterialstoreException {
        return generateDot(mProductGroup, Collections.emptyMap(), true);
    }


    //-----------------------------------------------------------------
    private static class MProductSubgraph {
        private final MaterialProduct materialProduct;
        private final GraphNodeId leftId;
        private final GraphNodeId rightId;
        private final GraphNodeId queryId;
        private final GraphNodeId diffId;
        public MProductSubgraph(MaterialProduct mp) throws MaterialstoreException {
            this.materialProduct = mp;
            this.leftId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(mp, Role.L);
            this.rightId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(mp, Role.R);
            this.queryId = GraphNodeIdResolver.resolveIdOfQueryInMaterialProduct(mp);
            this.diffId = GraphNodeIdResolver.resolveIdOfMaterialInMaterialProduct(mp, Role.D);
        }
        public GraphNodeId getLeftId() {
            return leftId;
        }
        public GraphNodeId getRightId() {
            return rightId;
        }
        public GraphNodeId getQueryId() { return queryId; }
        public GraphNodeId getDiffId() { return diffId; }
        public MaterialProduct getMaterialProduct() {
            return materialProduct;
        }
    }





    //-----------------------------------------------------------------
    private static String digraph(String content, String label) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("digraph G {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"" + label + "\"");
        pw.println(INDENT + INDENT + "rankdir=TB,");
        pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,sans-serif\"");
        pw.println(INDENT + "];");
        pw.println(INDENT + "node [");
        pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,Sans-serif\",");
        pw.println(INDENT + INDENT + "shape=plaintext");
        pw.println(INDENT + "];");
        pw.println(INDENT + "edge [");
        pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,Sans-serif\"");
        pw.println(INDENT + "];");
        List<String> lines = StringUtils.toList(content);
        lines.forEach(s -> pw.println(INDENT + s));
        pw.println("}");
        pw.flush();
        pw.close();
        return sw.toString();
    }


    /*
     * generate a PNG image from the given DOT text, return a BufferedImage
     */
    public static BufferedImage toImage(String dot) throws MaterialstoreException {
        try {
            Path dotFile = Files.createTempFile(null, null);
            Files.write(dotFile, dot.getBytes(StandardCharsets.UTF_8));
            Path pngFile = Files.createTempFile(null, null);
            int rc = runDotCommand(dotFile, pngFile);
            if (rc == 0) {
                return ImageIO.read(pngFile.toFile());
            } else {
                throw new MaterialstoreException("dot command failed to generate a PNG file");
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }


    /*
     * run the dot command of Graphviz in command line
     */
    public static int runDotCommand(Path dotFile, Path outFile) throws MaterialstoreException {
        try {
            Subprocess.CompletedProcess cp =
                    new Subprocess()
                            .run(Arrays.asList(
                                    "/usr/local/bin/dot",
                                    "-Tpng",
                                    "-o" + outFile.toString(),
                                    dotFile.toString()
                                    )
                            );
            if (cp.returncode() != 0) {
                cp.stdout().forEach(System.out::println);
                cp.stderr().forEach(System.err::println);
            }
            return cp.returncode();
        } catch (IOException | InterruptedException e) {
            throw new MaterialstoreException(e);
        }
    }

}
