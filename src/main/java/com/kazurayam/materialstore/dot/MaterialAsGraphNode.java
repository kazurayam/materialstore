package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;

public class MaterialAsGraphNode {

    private Material material;
    private GraphNodeId graphNodeId;

    private String INDENT = DotGenerator.INDENT;

    MaterialAsGraphNode(Material material, GraphNodeId graphNodeId) {
        this.material = material;
        this.graphNodeId = graphNodeId;
    }

    public String toGraphNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(graphNodeId.toString());
        sb.append(" ");
        sb.append("[label=<");
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f0\">");
        sb.append(escapeHTML(material.getShortId()));
        sb.append("</TD>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f1\">");
        sb.append(escapeHTML(material.getFileType().getExtension()));
        sb.append("</TD>\n");
        String json = material.getMetadata().toSimplifiedJson();
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f2\">");
        sb.append(insertBR(escapeBrace(escapeHTML(json))));
        sb.append("</TD>\n");
        sb.append(INDENT);
        sb.append("</TR></TABLE>");
        sb.append(">];");
        return sb.toString();
    }

    private static String escapeHTML(String str) {
        return str
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">","&gt;");
    }

    private static String escapeBrace(String str) {
        return str
                .replace("{", "{")
                .replace("}", "}");
    }

    private static String insertBR(String str) {
        return str.replace(", ", ",<BR/> ");
    }

}
