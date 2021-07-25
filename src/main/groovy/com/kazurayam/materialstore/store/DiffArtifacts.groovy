package com.kazurayam.materialstore.store

import java.util.stream.Collectors

class DiffArtifacts {

    private final List<DiffArtifact> diffArtifacts

    DiffArtifacts() {
        diffArtifacts = new ArrayList<DiffArtifact>()
    }

    void add(DiffArtifact e) {
        diffArtifacts.add(e)
    }

    DiffArtifact get(int index) {
        return diffArtifacts.get(index)
    }

    int size() {
        return diffArtifacts.size()
    }

    Iterator<DiffArtifact> iterator() {
        return diffArtifacts.iterator()
    }

    int countWarnings(Double criteria) {
        diffArtifacts.stream()
                .filter { DiffArtifact da ->
                    criteria < da.getDiffRatio()
                }
                .collect(Collectors.toList())
                .size()
    }

    void sort() {
        Collections.sort(diffArtifacts)
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        int count = 0
        sb.append("[")
        diffArtifacts.each {DiffArtifact da ->
            if (count > 0) sb.append(",")
            sb.append(da.toString())
            count += 1
        }
        sb.append("]")
        return sb.toString()
    }
}
