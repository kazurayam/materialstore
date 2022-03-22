package com.kazurayam.materialstore.filesystem;

public interface Jsonifiable {

    String toJson();

    String toJson(boolean prettyPrint);
}
