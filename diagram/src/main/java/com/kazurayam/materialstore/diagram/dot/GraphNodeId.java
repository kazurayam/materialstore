package com.kazurayam.materialstore.diagram.dot;

/**
 * the id of a graphviz node
 */
public class GraphNodeId {

    public static final GraphNodeId NULL_OBJECT = new GraphNodeId("");

    private final String value;

    public GraphNodeId(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GraphNodeId)) {
            return false;
        }
        GraphNodeId other = (GraphNodeId)obj;
        return other.getValue().equals(this.getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
