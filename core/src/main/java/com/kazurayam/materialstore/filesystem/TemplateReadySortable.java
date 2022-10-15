package com.kazurayam.materialstore.filesystem;

import java.util.Map;

public interface TemplateReadySortable extends TemplateReady {

    Map<String, Object> toTemplateModel(SortKeys sortKeys);

    String toTemplateModelAsJson(SortKeys sortKeys, boolean prettyPrint);
}
