package com.kazurayam.materialstore

interface MapLike {

    boolean containsKey(String key)

    String get(String key)

    boolean isEmpty()

    Set<String> keySet()

    int size()

}
