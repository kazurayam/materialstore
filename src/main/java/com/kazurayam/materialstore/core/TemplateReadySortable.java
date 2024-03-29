package com.kazurayam.materialstore.core;

import java.util.Map;

public interface TemplateReadySortable extends TemplateReady {

    Map<String, Object> toTemplateModel(SortKeys sortKeys);

    String toTemplateModelAsJson(SortKeys sortKeys, boolean prettyPrint);
}
