package com.kazurayam.materialstore.facet.textgrid

import com.google.gson.Gson

/**
 *
 */
class Values implements Comparable<Values> {
    private final List<String> values

    Values(List<String> values) {
        Objects.requireNonNull(values)
        this.values = values
    }

    Values(Values source) {
        this(copyStringList(source.values()))
    }

    private static List<String> copyStringList(List<String> source) {
        List<String> target = new ArrayList<>()
        for (String s in source) {
            target.add(s)
        }
        return target
    }

    List<String> values() {
        return this.values
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof Values)) {
            return false
        }
        Values other = (Values)obj
        boolean equality = true
        if (this.values().size() != other.values().size()) {
            return false
        }
        for (int i = 0; i < this.values().size(); i++) {
            if (this.values().get(i) != other.values().get(i)) {
                equality = false
                break
            }
        }
        return equality
    }

    @Override
    int hashCode() {
        int hash = 7
        for (int i = 0; i < this.values().size(); i++) {
            hash = 31 * hash + this.values().get(i).hashCode()
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
        for (String s in this.values()) {
            if (count > 0) {
                sb.append(", ")
            }
            sb.append(gson.toJson(s))
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }

    @Override
    int compareTo(Values other) {
        if (this.values().size() < other.values().size()) {
            return -1
        } else if (this.values().size() == other.values().size()) {
            for (int i = 0; i < this.values.size(); i++) {
                int comparison = this.values().get(i) <=> other.values().get(i)
                if (comparison != 0) {
                    return comparison
                }
            }
            return 0
        } else {
            return 1
        }
    }
}