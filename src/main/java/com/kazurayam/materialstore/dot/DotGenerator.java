package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.util.JsonUtil;
import com.kazurayam.materialstore.util.StringUtils;
import com.kazurayam.subprocessj.Subprocess;

import javax.imageio.ImageIO;
import javax.management.Query;
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
    /**
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
        GraphNodeId nodeId = GraphNodeIdResolver.getGraphNodeId(material);
        pw.println(new MaterialAsGraphNode(material, nodeId).toGraphNode());
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
    /**
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
                    GraphNodeIdResolver.getGraphNodeId(materialList, material);
            String nodeStmt = new MaterialAsGraphNode(material, nodeId).toGraphNode();
            pw.println(INDENT + nodeStmt);
        }
        // edges
        GraphNodeId precedingNodeId =
                GraphNodeIdResolver.getGraphNodeId(materialList, materialList.get(0));
        for (int i = 1; i < materialList.size(); i++) {
            GraphNodeId currentNodeId =
                    GraphNodeIdResolver.getGraphNodeId(materialList, materialList.get(i));
            pw.println(INDENT + precedingNodeId + ":f0" + " -> " + currentNodeId + ":f0" +  " [style=invis];");
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
    /**
     *
     */
    public static String generateDot(MaterialProduct materialProduct,
                                     Map<String, String> options, boolean standalone) throws MaterialstoreException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MP" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\""
                + materialProduct.getLeft().getJobName()
                + "/"
                + materialProduct.getLeft().getJobTimestamp()
                + " || "
                + materialProduct.getRight().getJobName()
                + "/"
                + materialProduct.getRight().getJobTimestamp()
                + "\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
        GraphNodeId leftGraphNodeId =
                GraphNodeIdResolver.getGraphNodeId(materialProduct, materialProduct.getLeft());
        GraphNodeId rightGraphNodeId =
                GraphNodeIdResolver.getGraphNodeId(materialProduct, materialProduct.getRight());
        // left Material
        String leftGraphNodeStatement =
                new MaterialAsGraphNode(materialProduct.getLeft(), leftGraphNodeId).toGraphNode();
        pw.println(INDENT + leftGraphNodeStatement);
        // query that connects the left and the right
        QueryOnMetadataAsGraphNode queryNode =
                new QueryOnMetadataAsGraphNode(
                        materialProduct.getQueryOnMetadata(),
                        leftGraphNodeId,
                        rightGraphNodeId);
        GraphNodeId queryNodeId = queryNode.getGraphNodeId();
        String queryNodeStatement = queryNode.toGraphNode();
        pw.println(INDENT + queryNodeStatement);
        // right Material
        String rightGraphNodeStatement =
                new MaterialAsGraphNode(materialProduct.getRight(), rightGraphNodeId).toGraphNode();
        pw.println(INDENT + rightGraphNodeStatement);
        // horizontal edge
        pw.println(INDENT + leftGraphNodeId + ":f2" + " -> "
                + queryNodeId + ":q0" + " [arrowhead=none];");
        pw.println(INDENT + queryNodeId + ":q0" + " -> "
                + rightGraphNodeId + ":f0" + " [arrowhead=none];");
        // rank
        pw.println(INDENT + "{rankdir=LR; rank=same; "
                + leftGraphNodeId + ", "
                + queryNodeId + ", "
                + rightGraphNodeId + ";}");
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
    public static String generateDotOfMPGBeforeZip(MProductGroup mProductGroup,
                                                   Map<String, String> options,
                                                   boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MPGBZ" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"MProductGroup " + mProductGroup.getShortId() + "\",");
        pw.println(INDENT + INDENT + "style=\"dashed\",");
        pw.println(INDENT + INDENT + "color=black");
        pw.println(INDENT + "];");
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

    public static String generateDotOfMPGBeforeZip(MProductGroup mProductGroup) {
        return generateDotOfMPGBeforeZip(mProductGroup, Collections.emptyMap(), true);
    }


    //-----------------------------------------------------------------
    /**
     *
     */
    public static String generateDot(MProductGroup mProductGroup,
                                     Map<String, String> options, boolean standalone)
            throws MaterialstoreException {
        //
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
        pw.println(INDENT + INDENT + "label=\"MProductGroup " + mProductGroup.getShortId() + "\",");
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
        GraphNodeId previousLeftMNodeId = mProductSubgraphList.get(0).getLeft();
        GraphNodeId previousRightMNodeId = mProductSubgraphList.get(0).getRight();
        for (int i = 1; i < mProductSubgraphList.size(); i++) {
            GraphNodeId currentLeftMNodeId = mProductSubgraphList.get(i).getLeft();
            GraphNodeId currentRightMNodeId = mProductSubgraphList.get(i).getRight();
            pw.println(INDENT + previousLeftMNodeId + ":f2" + " -> "
                    + currentLeftMNodeId + ":f2" + " [style=invis];");
            pw.println(INDENT + previousRightMNodeId + ":f2" + " -> "
                    + currentRightMNodeId + ":f2" + " [style=invis];");
            previousLeftMNodeId = currentLeftMNodeId;
            previousRightMNodeId = currentRightMNodeId;
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
    public static String generateDot(MProductGroup mProductGroup) throws MaterialstoreException {
        return generateDot(mProductGroup, Collections.emptyMap(), true);
    }


    //-----------------------------------------------------------------
    private static class MProductSubgraph {
        private final MaterialProduct materialProduct;
        private final GraphNodeId left;
        private final GraphNodeId right;
        public MProductSubgraph(MaterialProduct mp) throws MaterialstoreException {
            this.materialProduct = mp;
            this.left = GraphNodeIdResolver.getGraphNodeId(mp, mp.getLeft());
            this.right = GraphNodeIdResolver.getGraphNodeId(mp, mp.getRight());
        }
        public GraphNodeId getLeft() {
            return left;
        }
        public GraphNodeId getRight() {
            return right;
        }
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


    /**
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


    /**
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
