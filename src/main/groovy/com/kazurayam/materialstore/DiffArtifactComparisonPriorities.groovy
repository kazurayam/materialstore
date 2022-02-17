package com.kazurayam.materialstore

import com.google.gson.Gson

class DiffArtifactComparisonPriorities {

    static final DiffArtifactComparisonPriorities NULL_OBJECT = new DiffArtifactComparisonPriorities(new ArrayList<String>())

    private final List<String> arguments

    static DiffArtifactComparisonPriorities of(String ... args) {
        return new DiffArtifactComparisonPriorities(args)
    }

    private DiffArtifactComparisonPriorities(String ... args) {
        this(Arrays.asList(args))
    }

    private DiffArtifactComparisonPriorities(List<String> args) {
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
