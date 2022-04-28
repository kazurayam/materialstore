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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * generates a DOT script with which you can draw diagrams
 * of the objects using GraphViz
 */
public class DotGenerator {

    public static final String INDENT = "  ";


    /**
     * generate DOT of a Material object
     */
    public static String toDot(Material material,
                               Map<String, String> options, boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print("M");
        if (options.containsKey("MaterialListId")) {
            pw.print(options.get("MaterialListId") + "_");
        }
        pw.print(material.getShortId());
        pw.print(" [label=\"");
        pw.print(material.getShortId());
        pw.print("|");
        pw.print(material.getFileType().getExtension());
        pw.print("|");
        String json = material.getMetadata().toSimplifiedJson();
        String escaped =
                JsonUtil.escapeAsJsonString(json)
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace(",",",\\n");
        pw.print(escaped);
        pw.print("\"");
        if (options.containsKey("xlabel")) {
            pw.print(",xlabel=\"");
            pw.print(options.get("xlabel"));
            pw.print("\"");
        }
        pw.print("];");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraphTB(sw.toString());
        } else {
            return sw.toString();
        }
    }
    public static String toDot(Material material) {
        return toDot(material, Collections.emptyMap(), true);
    }
    public static String toDot(Material material, Map<String, String> options) {
        return toDot(material, options, true);
    }
    public static String toDot(Material material, boolean standalone) {
        return toDot(material, Collections.emptyMap(), standalone);
    }


    /**
     * generate DOT of a MaterialList object
     */
    public static String toDot(MaterialList materialList,
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
        for (Material material : materialList) {
            Map<String, String> materialOptions =
                    Collections.singletonMap("MaterialListId", materialList.getShortId());
            pw.println(INDENT + toDot(material, materialOptions,false));
        }

        // a lambda function that generates the ID of Material
        // "M" + the id of this MaterialList + "_" + the id of Material
        BiFunction<String, Material, String> getMaterialDotId =
                (materialListId, material) -> "M" + materialListId + "_" + material.getShortId();

        String prevId = getMaterialDotId.apply(materialList.getShortId(), materialList.get(0));
        for (int i = 1; i < materialList.size(); i++) {
            String currId = getMaterialDotId.apply(materialList.getShortId(), materialList.get(i));
            pw.println("  " + prevId + " -> " + currId + " [style=invis];");
            prevId = currId;
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
    public static String toDot(MaterialList materialList) {
        return toDot(materialList, Collections.emptyMap(), true);
    }
    public static String toDot(MaterialList materialList, Map<String, String> options) {
        return toDot(materialList, options, true);
    }
    public static String toDot(MaterialList materialList, boolean standalone) {
        return toDot(materialList, Collections.emptyMap(), standalone);
    }


    /**
     *
     */
    public static String toDot(MaterialProduct materialProduct,
                               Map<String, String> options, boolean standalone) {
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
        pw.println(INDENT + toDot(materialProduct.getLeft(),false));
        // right Material
        pw.println(INDENT + toDot(materialProduct.getRight(), false));
        // horizontal edge
        // TODO
        pw.println("}");
        pw.flush();
        pw.close();
        if (standalone) {
            return digraphTB(sw.toString());
        } else {
            return sw.toString();
        }
    }
    public static String toDot(MaterialProduct materialProduct) {
        return toDot(materialProduct, Collections.emptyMap(), true);
    }
    public static String toDot(MaterialProduct materialProduct, Map<String, String> options) {
        return toDot(materialProduct, options, true);
    }
    public static String toDot(MaterialProduct materialProduct, boolean standalone) {
        return toDot(materialProduct, Collections.emptyMap(), standalone);
    }



    public static String toDot(MProductGroup mProductGroup,
                               Map<String, String> options, boolean standalone) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String seq = options.getOrDefault("seq", "0");
        pw.println("subgraph cluster_MP" + seq + " {");
        pw.println("  label=\"MaterialProduct " + mProductGroup.getId() + "\"");
        pw.println("  color=green;");
        for (MaterialProduct mp : mProductGroup) {
            // left node
            if (mp.getLeft() != Material.NULL_OBJECT) {
                pw.print(StringUtils.indentLines(
                        toDot(mp.getLeft(),
                                Collections.singletonMap("xlabel", "Left"),
                                false)));
            }
            // right node
            if (mp.getRight() != Material.NULL_OBJECT) {
                pw.print(StringUtils.indentLines(
                        toDot(mp.getRight(),
                                Collections.singletonMap("xlabel", "Right"),
                                false)));
            }
            // edge
            throw new RuntimeException("TODO");
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
    public static String toDot(MProductGroup mProductGroup) {
        return toDot(mProductGroup, Collections.emptyMap(), true);
    }
    public static String toDot(MProductGroup mProductGroup, Map<String, String> options) {
        return toDot(mProductGroup, options, true);
    }
    public static String toDot(MProductGroup mProductGroup, boolean standalone) {
        return toDot(mProductGroup, Collections.emptyMap(), standalone);
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
            //pw.println(IND + "rankdir=" + rankdir + ",");
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
    public static BufferedImage toDiagram(String dot) throws MaterialstoreException {
        try {
            Path dotFile = Files.createTempFile(null, null);
            Files.write(dotFile, dot.getBytes(StandardCharsets.UTF_8));
            Path pngFile = Files.createTempFile(null, null);
            runDot(dotFile, pngFile);
            return ImageIO.read(pngFile.toFile());
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }


    /**
     * run the dot command of Graphviz in command line
     */
    public static int runDot(Path dotFile, Path outFile) throws MaterialstoreException {
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
