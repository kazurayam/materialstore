package com.kazurayam.materialstore.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class GsonHelper {

    static Gson createGson(boolean prettyPrint) {
        if (prettyPrint) {
            return new GsonBuilder().setPrettyPrinting().create()
        } else {
            return new Gson()
        }
    }

}
