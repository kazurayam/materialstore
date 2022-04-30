package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MaterialProduct;
import com.kazurayam.materialstore.util.JsonUtil;
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

    public static final String INDENT = "  ";

    public static String toNodeStmt(GraphNodeId nodeId,
                                    Material material) {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeId.toString());
        sb.append(" ");
        sb.append("[label=\"");
        sb.append(material.getShortId());
        sb.append("|");
        sb.append(material.getFileType().getExtension());
        sb.append("|");
        String json = material.getMetadata().toSimplifiedJson();
        String escaped = JsonUtil.escapeAsJsonString(json)
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace(",",",\\n");
        sb.append(escaped);
        sb.append("\"");
        sb.append("];");
        return sb.toString();
    }

    /**
     * generate DOT of a Material object
     */
    public static String generateDot(Material material,
                                     Map<String, String> options, boolean standalone) {
        StringWriter sw = new StringWriter();
        MaterialSolo solo = new MaterialSolo(material);
        GraphNodeId nodeId = solo.getGraphNodeId();
        sw.append(toNodeStmt(nodeId, material));
        if (standalone) {
            return digraphTB(sw.toString());
        } else {
            return sw.toString();
        }
    }

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
        pw.println(INDENT + INDENT + "color=blue");
        pw.println(INDENT + "];");
        pw.println(INDENT + "node [");
        pw.println(INDENT + INDENT + "style=filled");
        pw.println(INDENT + "];");
        // nodes
        for (Material material : materialList) {
            GraphNodeId nodeId = new MaterialInMaterialList(materialList, material).getGraphNodeId();
            String nodeStmt = toNodeStmt(nodeId, material);
            pw.println(INDENT + nodeStmt);
        }
        // edges
        GraphNodeId precedingNodeId =
                new MaterialInMaterialList(materialList, materialList.get(0))
                        .getGraphNodeId();
        for (int i = 1; i < materialList.size(); i++) {
            GraphNodeId currentNodeId =
                    new MaterialInMaterialList(materialList, materialList.get(i))
                            .getGraphNodeId();
            pw.println(INDENT + precedingNodeId + " -> " + currentNodeId + " [style=invis];");
            precedingNodeId = currentNodeId;
        }
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraphTB(sw.toString());
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MaterialList materialList) {
        return generateDot(materialList, Collections.emptyMap(), true);
    }


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
        pw.println(INDENT + INDENT + "label=\"" + materialProduct.getReducedTimestamp().toString()
                + "/" + materialProduct.getFileTypeExtension()
                + "/" + JsonUtil.escapeAsJsonString(materialProduct.getQueryOnMetadata().toJson())
                .replace("{", "\\{")
                .replace("}", "\\}")
                + "\",");
        pw.println(INDENT + INDENT + "color=red");
        pw.println(INDENT + "];");
        pw.println(INDENT + "node [");
        pw.println(INDENT + INDENT + "style=filled");
        pw.println(INDENT + "];");
        // left Material
        GraphNodeId leftNodeId =
                new MaterialInMaterialProduct(materialProduct, materialProduct.getLeft()).getGraphNodeId();
        String leftNodeStmt = toNodeStmt(leftNodeId, materialProduct.getLeft());
        pw.println(INDENT + leftNodeStmt);
        // right Material
        GraphNodeId rightNodeId =
                new MaterialInMaterialProduct(materialProduct, materialProduct.getRight()).getGraphNodeId();
        String rightNodeStmt = toNodeStmt(rightNodeId, materialProduct.getRight());
        pw.println(INDENT + rightNodeStmt);

        // horizontal edge
        pw.println(INDENT + leftNodeId + " -> " + rightNodeId + " [arrowhead=none];");
        pw.println("}");

        pw.flush();
        pw.close();
        if (standalone) {
            return digraphLR(sw.toString());
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MaterialProduct materialProduct) throws MaterialstoreException {
        return generateDot(materialProduct, Collections.emptyMap(), true);
    }


    public static String generateDotOfMPGBeforeZip(MProductGroup mProductGroup,
                                                             Map<String, String> options,
                                                             boolean standalone)
            throws MaterialstoreException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MPGBZ" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"MProductGroup " + mProductGroup.getId() + "\",");
        pw.println(INDENT + INDENT + "color=green");
        pw.println(INDENT + "];");
        pw.println(INDENT + "node [");
        pw.println(INDENT + INDENT + "style=filled");
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
            return digraphTB(sw.toString());
        } else {
            return sw.toString();
        }
    }

    public static String generateDotOfMPGBeforeZip(MProductGroup mProductGroup)
            throws MaterialstoreException {
        return generateDotOfMPGBeforeZip(mProductGroup, Collections.emptyMap(), true);
    }


    /**
     *
     */
    public static String generateDot(MProductGroup mProductGroup,
                                     Map<String, String> options, boolean standalone)
            throws MaterialstoreException {
        // make a grid of NodeIds
        List<MProductSubgraph> nodeIdGrid = new ArrayList<>();
        for (MaterialProduct mp : mProductGroup) {
            MProductSubgraph nodeIdPair = new MProductSubgraph(mp);
            nodeIdGrid.add(nodeIdPair);
        }
        //
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sequenceNumber = options.getOrDefault("sequenceNumber", "0");
        pw.println("subgraph cluster_MPG" + sequenceNumber + " {");
        pw.println(INDENT + "graph [");
        pw.println(INDENT + INDENT + "label=\"MProductGroup " + mProductGroup.getId() + "\",");
        pw.println(INDENT + INDENT + "color=green");
        pw.println(INDENT + "];");
        pw.println(INDENT + "node [");
        pw.println(INDENT + INDENT + "style=filled");
        pw.println(INDENT + "];");
        // nodes
        int index = 0;
        for (MProductSubgraph nodeIdPair: nodeIdGrid) {
            GraphNodeId leftId = nodeIdPair.getLeft();
            GraphNodeId rightId = nodeIdPair.getRight();
            MaterialProduct mp = nodeIdPair.getMaterialProduct();
            Map<String, String> opt = Collections.singletonMap("sequenceNumber", String.valueOf(index));
            pw.println(INDENT + generateDot(mp, opt, false));
            index += 1;
        }
        // edges
        GraphNodeId previousLeftMNodeId = nodeIdGrid.get(0).getLeft();
        GraphNodeId previousRightMNodeId = nodeIdGrid.get(0).getRight();
        pw.println(previousLeftMNodeId + " -> " + previousRightMNodeId + " [arrowhead=none];");
        for (int i = 1; i < nodeIdGrid.size(); i++) {
            GraphNodeId currentLeftMNodeId = nodeIdGrid.get(i).getLeft();
            GraphNodeId currentRightMNodeId = nodeIdGrid.get(i).getRight();
            pw.println(currentLeftMNodeId + " -> " + currentRightMNodeId + " [arrowhead=none];");
            pw.println(previousLeftMNodeId + " -> " + currentLeftMNodeId + " [style=invis];");
            pw.println(previousRightMNodeId + " -> " + currentRightMNodeId + " [style=invis];");
        }
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraphLR(sw.toString());
        } else {
            return sw.toString();
        }
    }
    public static String generateDot(MProductGroup mProductGroup) throws MaterialstoreException {
        return generateDot(mProductGroup, Collections.emptyMap(), true);
    }

    private static class MProductSubgraph {
        private MaterialProduct materialProduct;
        private GraphNodeId left;
        private GraphNodeId right;
        public MProductSubgraph(MaterialProduct mp) throws MaterialstoreException {
            this.materialProduct = mp;
            this.left = new MaterialInMaterialProduct(mp, mp.getLeft()).getGraphNodeId();
            this.right = new MaterialInMaterialProduct(mp, mp.getRight()).getGraphNodeId();
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

    public static String digraphTB(String content) {
        return digraph(content, "TB");
    }

    public static String digraphLR(String content) {
        return digraph(content, "LR");
    }

    private static String digraph(String content, String rankdir) {
        if (rankdir.equals("TB") || rankdir.equals("LR")) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("digraph G {");
            pw.println(INDENT + "graph [");
            pw.println(INDENT + "rankdir=" + rankdir + ",");
            pw.println(INDENT + INDENT + "concentrate=True,");
            pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,sans-serif\"");
            pw.println(INDENT + "];");
            pw.println(INDENT + "node [");
            pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,Sans-serif\",");
            pw.println(INDENT + INDENT + "shape=record");
            pw.println(INDENT + "];");
            pw.println(INDENT + "edge [");
            pw.println(INDENT + INDENT + "fontname=\"Helvetica,Arial,Sans-serif\"");
            pw.println(INDENT + "];");
            List<String> lines = StringUtils.toList(content);
            lines.stream().forEach(s -> pw.println(INDENT + s));
            pw.println("}");
            pw.flush();
            pw.close();
            return sw.toString();
        } else {
            throw new IllegalArgumentException("rankdir=" + rankdir + " is invalid");
        }
    }


    /**
     * generate a PNG image from the given DOT text, return a BufferedImage
     */
    public static BufferedImage toImage(String dot) throws MaterialstoreException {
        try {
            Path dotFile = Files.createTempFile(null, null);
            Files.write(dotFile, dot.getBytes(StandardCharsets.UTF_8));
            Path pngFile = Files.createTempFile(null, null);
            runDotCommand(dotFile, pngFile);
            return ImageIO.read(pngFile.toFile());
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
