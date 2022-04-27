package com.kazurayam.materialstore.filesystem;

public interface GraphvizReady {

    String toDot();

    String toDot(boolean standalone);

    String getDotId();

}
