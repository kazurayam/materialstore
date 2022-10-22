package com.kazurayam.materialstore.filesystem;

import java.io.IOException;

public interface Jsonifiable {

    String toJson();

    String toJson(boolean prettyPrint);
}
