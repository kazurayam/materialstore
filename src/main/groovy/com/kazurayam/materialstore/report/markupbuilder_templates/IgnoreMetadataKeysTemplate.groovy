package com.kazurayam.materialstore.report.markupbuilder_templates

import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder

class IgnoreMetadataKeysTemplate {

    private final IgnoreMetadataKeys ignoreMetadataKeys

    IgnoreMetadataKeysTemplate(IgnoreMetadataKeys ignoreMetadataKeys) {
        this.ignoreMetadataKeys = ignoreMetadataKeys
    }

    void toSpanSequence(MarkupBuilder mb) {
        List<String> list = new ArrayList<String>(ignoreMetadataKeys.keySet())
        Collections.sort(list)
        int count = 0
        mb.span("[")
        list.each {
            if (count > 0) {
                mb.span(", ")
            }
            mb.span(class: "ignored-key",
                    "\"" + JsonUtil.escapeAsJsonString(it) + "\"")
            count += 1
        }
        mb.span("]")
    }
}
