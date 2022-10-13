package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.filesystem.metadata.SortKeys;

import java.util.Map;

public interface TemplateReadySortable extends TemplateReady {

    Map<String, Object> toTemplateModel(SortKeys sortKeys);

}
