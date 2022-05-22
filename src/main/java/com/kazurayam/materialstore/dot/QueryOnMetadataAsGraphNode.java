package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.QueryOnMetadata;

public class QueryOnMetadataAsGraphNode extends AbstractGraphNode {

    private final QueryOnMetadata query;
    private final GraphNodeId leftMaterialGraphNodeId;
    private final GraphNodeId rightMaterialGraphNodeId;

    private static final String INDENT = DotGenerator.INDENT;

    public QueryOnMetadataAsGraphNode(QueryOnMetadata query,
                                      GraphNodeId leftMaterialGraphNodeId,
                                      GraphNodeId rightMaterialGraphNodeId) {
        this.query = query;
        this.leftMaterialGraphNodeId = leftMaterialGraphNodeId;
        this.rightMaterialGraphNodeId = rightMaterialGraphNodeId;
    }

    public GraphNodeId getGraphNodeId() {
        return new GraphNodeId("QUERY_"
                + leftMaterialGraphNodeId.getValue()
                + "_"
                + rightMaterialGraphNodeId.getValue() );
    }

    public String toGraphNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getGraphNodeId().getValue());
        sb.append(" ");
        sb.append("[label=<");
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"q0\">");
        sb.append(insertBR(escapeHTML(query.toJson())));
        sb.append("</TD>\n");
        sb.append(INDENT);
        sb.append("</TR></TABLE>");
        sb.append(">];");
        return sb.toString();
    }


}
