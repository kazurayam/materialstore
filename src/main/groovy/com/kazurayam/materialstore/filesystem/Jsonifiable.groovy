package com.kazurayam.materialstore.filesystem

interface Jsonifiable {

    String toJson()

    String toJson(boolean prettyPrint)

}
