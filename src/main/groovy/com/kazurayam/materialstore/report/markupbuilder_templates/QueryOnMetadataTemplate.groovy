package com.kazurayam.materialstore.report.markupbuilder_templates

import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import groovy.xml.MarkupBuilder

class QueryOnMetadataTemplate {
    static void toSpanSequence(QueryOnMetadata query, MarkupBuilder mb) {
        List<String> keyList = new ArrayList(query.keySet())
        Collections.sort(keyList)
        int count = 0
        mb.span("{")
        keyList.forEach( { String key ->
            if (count > 0) {
                mb.span(", ")
            }
            mb.span("\"${key.toString()}\":")
            mb.span("\"" + query.getAsString(key) + "\"")
            count += 1
        })
        mb.span("}")
    }
}
