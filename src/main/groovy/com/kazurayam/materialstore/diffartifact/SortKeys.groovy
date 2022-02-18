package com.kazurayam.materialstore.diffartifact

import com.google.gson.Gson

class SortKeys {

    public static final SortKeys NULL_OBJECT = new SortKeys(new ArrayList<String>())

    private final List<String> arguments

    static SortKeys of(String ... args) {
        return new SortKeys(args)
    }

    private SortKeys(String ... args) {
        this(Arrays.asList(args))
    }

    private SortKeys(List<String> args) {
        this.arguments = args
    }

    Iterator<String> iterator() {
        arguments.iterator()
    }

    int size() {
        return arguments.size()
    }

    String get(int index) {
        return arguments.get(index)
    }

    @Override
    String toString() {
        Gson gson = new Gson()
        StringBuilder sb = new StringBuilder()
        sb.append("[")
        arguments.eachWithIndex { arg, index ->
            if (index > 0) {
                sb.append(", ")
            }
            sb.append(gson.toJson(arg))
        }
        sb.append("]")
        return sb.toString()
    }
}
