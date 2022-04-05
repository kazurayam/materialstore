package com.kazurayam.materialstore.facet.textgrid;

import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class Row implements Comparable<Row> {

    private final Values values;
    private final KeyRange keyRange;
    private final Key key;

    public Row(List<String> row, KeyRange keyRange) {
        Objects.requireNonNull(row);
        Objects.requireNonNull(keyRange);
        validateKeyRange(row, keyRange);
        this.values = new Values(row);
        this.keyRange = keyRange;
        this.key = new Key(row, keyRange);
    }

    private static void validateKeyRange(List<String> row, KeyRange keyRange) {
        assert keyRange.getFrom() <= keyRange.getTo();
        assert 0 <= keyRange.getFrom();
        assert keyRange.getFrom() < row.size();
        assert 0 <= keyRange.getTo();
        assert keyRange.getTo() < row.size();
    }

    public Values values() {
        return this.values;
    }

    public KeyRange keyRange() {
        return this.keyRange;
    }

    public Key key() {
        return this.key;
    }

    public boolean keyEquals(Row other) {
        return this.key().equals(other.key());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Row)) {
            return false;
        }

        Row other = (Row) obj;
        return this.values().equals(other.values()) && this.key().equals(other.key());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.values().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(gson.toJson("key"));
        sb.append(":");
        sb.append(this.key().toJson());
        sb.append(",");
        sb.append(gson.toJson("values"));
        sb.append(":");
        sb.append(this.values().toJson());
        sb.append(",");
        sb.append(gson.toJson("keyRange"));
        sb.append(":");
        sb.append(keyRange().toJson());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(Row other) {
        int keyComparison = this.key().compareTo(other.key());
        if (keyComparison == 0) {
            return this.values().compareTo(other.values());
        } else {
            return keyComparison;
        }

    }


}
