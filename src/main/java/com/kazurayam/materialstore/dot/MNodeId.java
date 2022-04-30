package com.kazurayam.materialstore.dot;

/**
 * the id of a graphviz node
 */
public class MNodeId {

    public static final MNodeId NULL_OBJECT = new MNodeId("");

    private final String value;

    public MNodeId(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MNodeId)) {
            return false;
        }
        MNodeId other = (MNodeId)obj;
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
