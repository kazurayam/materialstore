package com.kazurayam.materialstore.textgrid

import com.google.gson.Gson

/**
 *
 */
class Key implements Comparable<Key> {
    private final List<String> keyElements = new ArrayList<>()
    private final Range<Integer> keyRange

    Key(List<String> row, Range<Integer> keyRange) {
        Objects.requireNonNull(row)
        Objects.requireNonNull(keyRange)
        validateParams(row, keyRange)
        this.keyElements.addAll(getKeyElements(row, keyRange))
        this.keyRange = keyRange
    }

    Key(Key source) {
        this(source.keyElements(), source.keyRange())
    }

    private static void validateParams(List<String> row, Range<Integer> keyRange) {
        assert 0 <= row.size()
        assert 0 <= keyRange.getFrom()
        assert keyRange.getFrom() <= keyRange.getTo()
        assert keyRange.getTo() < row.size()
    }

    private static List<String> getKeyElements(List<String> row, Range<Integer> keyRange) {
        List<String> keyElements = new ArrayList<>()
        for (int i = keyRange.getFrom(); i <= keyRange.getTo(); i++) {
            keyElements.add(row.get(i))
        }
        return keyElements
    }

    List<String> keyElements() {
        return this.keyElements
    }

    Range keyRange() {
        return this.keyRange
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof Key)) {
            return false
        }
        Key other = (Key)obj
        if (this.keyElements().size() != other.keyElements().size()) {
            return false
        }
        boolean equality = true
        for (int i = 0; i < this.keyElements().size(); i++) {
            if (this.keyElements().get(i) !=  other.keyElements().get(i)) {
                equality = false
                break
            }
        }
        return equality
    }

    @Override
    int hashCode() {
        int hash = 7
        for (int i = 0; i < this.keyElements().size(); i++) {
            hash = 31 * hash + this.keyElements().get(i).hashCode()
        }
        return hash
    }

    @Override
    String toString() {
        return toJson()
    }

    String toJson() {
        Gson gson = new Gson()
        StringBuilder sb = new StringBuilder()
        sb.append("[")
        int count = 0
        for (String s in this.keyElements()) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(gson.toJson(s))
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }

    @Override
    int compareTo(Key other) {
        List<String> tke = this.keyElements()
        List<String> oke = other.keyElements()
        if (tke.size() < oke.size()) {
            return -1
        } else if (tke.size() == oke.size()) {
            for (int i = 0; i < tke.size(); i++) {
                int comparison = tke.get(i) <=> oke.get(i)
                if (comparison != 0) {
                    return comparison
                }
            }
            return 0
        } else {
            return +1
        }
    }
}