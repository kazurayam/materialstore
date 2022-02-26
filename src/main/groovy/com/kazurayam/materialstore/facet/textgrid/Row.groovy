package com.kazurayam.materialstore.facet.textgrid

import com.google.gson.Gson

/**
 *
 */
class Row implements Comparable<Row> {
    private final Values values
    private final Range<Integer> keyRange
    private final Key key

    Row(List<String> row, Range<Integer> keyRange) {
        Objects.requireNonNull(row)
        Objects.requireNonNull(keyRange)
        validateKeyRange(row, keyRange)
        this.values = new Values(row)
        this.keyRange = keyRange
        this.key = new Key(row, keyRange)
    }

    Row(Row source) {
        this(source.values().values(), source.keyRange())
    }

    private static void validateKeyRange(List<String> row, Range<Integer> keyRange) {
        assert keyRange.getFrom() <= keyRange.getTo()
        assert 0 <= keyRange.getFrom()
        assert keyRange.getFrom() < row.size()
        assert 0 <= keyRange.getTo()
        assert keyRange.getTo() < row.size()
    }

    Values values() {
        return this.values
    }

    Range<Integer> keyRange() {
        return this.keyRange
    }

    Key key() {
        return this.key
    }

    boolean keyEquals(Row other) {
        return this.key() == other.key()
    }


    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof Row)) {
            return false
        }
        Row other = (Row)obj
        return this.values() == other.values() &&
                this.key() == other.key()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.values().hashCode()
        return hash
    }

    @Override
    String toString() {
        return toJson()
    }

    String toJson() {
        Gson gson = new Gson()
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append(gson.toJson("key") + ":" + this.key().toJson())
        sb.append(",")
        sb.append(gson.toJson("values") + ":" + this.values().toJson())
        sb.append(",")
        sb.append(gson.toJson("keyRange") + ":" + this.keyRange())
        sb.append("}")
        return sb.toString()
    }

    @Override
    int compareTo(Row other) {
        int keyComparison = this.key() <=> other.key()
        if (keyComparison == 0) {
            int valuesComparison = this.values() <=> other.values()
            return valuesComparison
        } else {
            return keyComparison
        }
    }
}