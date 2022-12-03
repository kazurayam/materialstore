package com.kazurayam.materialstore.facet.textgrid;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public final class Key implements Comparable<Key> {

    private final List<String> keyElements = new ArrayList<>();
    private final KeyRange keyRange;

    public Key(List<String> row, KeyRange keyRange) {
        Objects.requireNonNull(row);
        Objects.requireNonNull(keyRange);
        validateParams(row, keyRange);
        this.keyElements.addAll(getKeyElements(row, keyRange));
        this.keyRange = keyRange;
    }

    private static void validateParams(List<String> row, KeyRange keyRange) {
        assert 0 <= keyRange.getFrom();
        assert keyRange.getFrom() <= keyRange.getTo();
        assert keyRange.getTo() < row.size();
    }

    private static List<String> getKeyElements(List<String> row, KeyRange keyRange) {
        List<String> keyElements = new ArrayList<>();
        for (int i = keyRange.getFrom(); i <= keyRange.getTo() ; i++) {
            keyElements.add(row.get(i));
        }

        return keyElements;
    }

    public List<String> keyElements() {
        return this.keyElements;
    }

    public KeyRange keyRange() {
        return this.keyRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Key)) {
            return false;
        }

        Key other = (Key) obj;
        if (this.keyElements().size() != other.keyElements().size()) {
            return false;
        }

        boolean equality = true;
        for (int i = 0; i < this.keyElements().size() ; i++) {
            if (!this.keyElements().get(i).equals(other.keyElements().get(i))) {
                equality = false;
                break;
            }

        }

        return equality;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < this.keyElements().size() ; i++){
            hash = 31 * hash + this.keyElements().get(i).hashCode();
        }

        return hash;
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int count = 0;
        for (String s : this.keyElements()) {
            if (count > 0) {
                sb.append(",");
            }

            sb.append(gson.toJson(s));
            count += 1;
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(Key other) {
        List<String> tke = this.keyElements();
        List<String> oke = other.keyElements();
        if (tke.size() < oke.size()) {
            return -1;
        } else if (tke.size() == oke.size()) {
            for (int i = 0; i < tke.size() ; i++){
                int comparison = tke.get(i).compareTo(oke.get(i));
                if (comparison != 0) {
                    return comparison;
                }

            }

            return 0;
        } else {
            return +1;
        }
    }
}
