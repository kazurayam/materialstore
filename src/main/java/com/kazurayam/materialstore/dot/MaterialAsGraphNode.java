package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;

public class MaterialAsGraphNode extends AbstractGraphNode {

    private Material material;
    private GraphNodeId graphNodeId;

    private String INDENT = DotGenerator.INDENT;

    MaterialAsGraphNode(Material material, GraphNodeId graphNodeId) {
        this.material = material;
        this.graphNodeId = graphNodeId;
    }

    public String toGraphNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(graphNodeId.getValue());
        sb.append(" ");
        sb.append("[label=<");
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"");
        if (material.getShortId().equals("0000000")) {
            sb.append(" BGCOLOR=\"gold\"");
        }
        sb.append("><TR>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f0\">");
        sb.append(escapeHTML(material.getShortId()));
        sb.append("</TD>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f1\">");
        sb.append(escapeHTML(material.getFileType().getExtension()));
        sb.append("</TD>\n");
        String json = material.getMetadata().toSimplifiedJson();

        System.out.println(material.getMetadata().toJson() + "\n");

        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f2\">");
        sb.append(insertBR(escapeHTML(json)));
        sb.append("</TD>\n");
        sb.append(INDENT);
        sb.append("</TR></TABLE>");
        sb.append(">];");
        return sb.toString();
    }

}
