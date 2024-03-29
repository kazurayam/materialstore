package com.kazurayam.materialstore.core;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kazurayam.materialstore.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.Map;

public interface TemplateReady extends Jsonifiable {

    /**
     * convert the object into Map object which can be consumed by FreeMarker as model
     *
     * @return a model
     */
    //Map<String, Object> toTemplateModel();
    default Map<String, Object> toTemplateModel() {
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        return new Gson().fromJson(toJson(), mapType);
    }

    /**
     * @return one line json (non-pretty-printed)
     */
    default String toTemplateModelAsJson() {
        return toTemplateModelAsJson(false);
    }

    /*
     * for debugging purposes.
     * <p>
     * turn the object returned by toTemplateModel() into a pretty-printed JSON text string
     *
     * @return JSON string
     */
    //String toTemplateModelAsJson(boolean prettyPrint);
    default String toTemplateModelAsJson(boolean prettyPrint) {
        Gson gson = GsonHelper.createGson(prettyPrint);
        Map<String, Object> model = toTemplateModel();
        return gson.toJson(model);
    }
}
