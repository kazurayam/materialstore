package com.kazurayam.materialstore.filesystem;

import java.util.Map;

public interface GraphvizReady {

    String toDot();

    String toDot(Map<String, String> options);

    String toDot(boolean standalone);

    String toDot(Map<String, String> options, boolean standalone);

}
