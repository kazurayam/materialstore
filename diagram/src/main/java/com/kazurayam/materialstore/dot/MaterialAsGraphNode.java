package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaterialAsGraphNode extends AbstractGraphNode {

    private GraphNodeId graphNodeId;
    private Material material;

    private String INDENT = DotGenerator.INDENT;

    MaterialAsGraphNode(GraphNodeId graphNodeId, Material material) {
        this.material = material;
        this.graphNodeId = graphNodeId;
    }

    public String toGraphNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(graphNodeId.getValue());
        sb.append(" ");
        sb.append("[label=<");
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"");
        if (material.getShortID().equals("0000000")) {
            sb.append(" BGCOLOR=\"gold\"");
        }
        sb.append("><TR>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f0\">");
        sb.append(escapeHTML(material.getShortID()));
        sb.append("</TD>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f1\">");
        sb.append(escapeHTML(material.getFileType().getExtension()));
        sb.append("</TD>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"f2\">");
        sb.append(formatMetadata());
        sb.append("</TD>\n");
        sb.append(INDENT);
        sb.append("</TR></TABLE>");
        sb.append(">];");
        return sb.toString();
    }

    String formatMetadata() {
        //return insertBR(escapeHTML(this.material.getMetadata().toSimplifiedJson()));
        Metadata metadata = this.material.getMetadata();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        List<String> keyList = new ArrayList<>(metadata.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            if (count > 0) {
                sb.append(", ");
                sb.append("<BR />");
            }
            MetadataAttribute metadataAttribute = metadata.getMetadataAttribute(key);
            if (metadataAttribute.isIgnoredByKey()) {
                sb.append("<S>");
                sb.append(escapeHTML("\"" + metadataAttribute.getKey() + "\""));
                sb.append("</S>");
            } else {
                sb.append(escapeHTML("\"" + metadataAttribute.getKey() + "\""));
            }
            sb.append(":");
            if (metadataAttribute.isIdentifiedByValue()) {
                sb.append("<B><FONT color=\"forestgreen\">");
                sb.append(escapeHTML("\"" + metadataAttribute.getValue() + "\""));
                sb.append("</FONT></B>");
            } else {
                sb.append(escapeHTML("\"" + metadataAttribute.getValue() + "\""));
            }
            count += 1;
        }
        sb.append("}");
        return sb.toString();
    }
    
}
