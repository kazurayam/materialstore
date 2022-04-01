package com.kazurayam.materialstore.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class GsonHelper {
    public static Gson createGson(boolean prettyPrint) {
        if (prettyPrint) {
            return new GsonBuilder().setPrettyPrinting().create();
        } else {
            return new Gson();
        }

    }

    public static Map<String, String> toStringStringMap(Map<?, ?> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put((entry.getKey()).toString(), (entry.getValue()).toString());
        }

        return result;
    }

}
