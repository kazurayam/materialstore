package com.kazurayam.materialstore.filesystem

interface JSONifiable {

    String toJson()

    String toJson(boolean prettyPrint)

}
