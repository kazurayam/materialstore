package com.kazurayam.materialstore.dot;

import com.kazurayam.materialstore.filesystem.QueryOnMetadata;

public class QueryAsGraphNode extends AbstractGraphNode {

    private final GraphNodeId id;
    private final QueryOnMetadata query;

    private static final String INDENT = DotGenerator.INDENT;

    public QueryAsGraphNode(GraphNodeId id, QueryOnMetadata query) {
        this.id = id;
        this.query = query;
    }

    public String toGraphNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id.getValue());
        sb.append(" ");
        sb.append("[label=<");
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"gray50\"><TR>\n");
        sb.append(INDENT + INDENT);
        sb.append("<TD PORT=\"q0\"><FONT color=\"white\">");
        sb.append(insertBR(escapeHTML(query.toJson())));
        sb.append("</FONT></TD>\n");
        sb.append(INDENT);
        sb.append("</TR></TABLE>");
        sb.append(">];");
        return sb.toString();
    }


}
