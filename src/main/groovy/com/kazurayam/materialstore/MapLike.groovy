package com.kazurayam.materialstore

interface MapLike {

    boolean containsKey(String key)

    Object get(String key)

    boolean isEmpty()

    Set<String> keySet()

    int size()

}
