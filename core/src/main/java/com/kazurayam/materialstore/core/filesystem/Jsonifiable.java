package com.kazurayam.materialstore.core.filesystem;

import java.io.IOException;

public interface Jsonifiable {

    String toJson();

    String toJson(boolean prettyPrint);
}
