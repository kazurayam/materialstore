package com.kazurayam.materialstore.core.filesystem;

public interface Jsonifiable {

    String toJson();

    String toJson(boolean prettyPrint);
}
