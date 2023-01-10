package com.kazurayam.materialstore.core;

public interface Jsonifiable {

    String toJson();

    String toJson(boolean prettyPrint);
}
