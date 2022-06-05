package com.kazurayam.materialstore.facet.textgrid;

public final class KeyRange {
    private int from;
    private int to;
    public KeyRange(int from, int to) {
        if (from < 0) {
            throw new IllegalArgumentException("from must not be negative");
        }
        if (to < 0) {
            throw new IllegalArgumentException("to must not be negative");
        }
        if (from > to) {
            throw new IllegalArgumentException("from=" + from + ", to=" + to);
        }
        this.from = from;
        this.to = to;
    }
    public int getFrom() {
        return from;
    }
    public int getTo() {
        return to;
    }
    public String toJson() {
        return "[" + from + ", " + to + "]";
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeyRange)) {
            return false;
        }
        KeyRange other = (KeyRange)obj;
        return this.getFrom() == other.getFrom() &&
                this.getTo() == other.getTo();
    }
}
