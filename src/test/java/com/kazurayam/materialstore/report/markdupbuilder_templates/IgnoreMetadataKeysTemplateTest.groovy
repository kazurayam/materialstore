package com.kazurayam.materialstore.report.markdupbuilder_templates


import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys

import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

class IgnoreMetadataKeysTemplateTest extends AbstractTemplateTest {

    @Test
    void test_toSpanSequence() {
        IgnoreMetadataKeys ignoreMetadataKeys =
                new IgnoreMetadataKeys.Builder()
                        .ignoreKeys("URL.protocol", "URL.port")
                        .build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new IgnoreMetadataKeysTemplate(ignoreMetadataKeys).toSpanSequence(mb)
        String markup = sw.toString()
        /*
<span>{</span>
<span class='ignored-key'>"URL.port"</span>
<span>, </span>
<span class='ignored-key'>"URL.protocol"</span>
<span>}</span>
         */
        println markup

    }

}