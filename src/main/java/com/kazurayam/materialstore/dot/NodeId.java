package com.kazurayam.materialstore.dot;

/**
 * the id of a graphviz node
 */
public class NodeId {

    public static final NodeId NULL_OBJECT = new NodeId("");

    private String value;

    public NodeId(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodeId)) {
            return false;
        }
        NodeId other = (NodeId)obj;
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
