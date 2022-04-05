package com.kazurayam.materialstore.report.markdupbuilder_templates

import com.kazurayam.materialstore.filesystem.QueryOnMetadata

import groovy.xml.MarkupBuilder
import org.junit.jupiter.api.Test

class QueryOnMetadataTemplateTest extends AbstractTemplateTest {


    @Test
    void test_toSpanSequence() {
        QueryOnMetadata query = QueryOnMetadata.builder(metadata0).build()
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        new QueryOnMetadataTemplate(query).toSpanSequence(mb)
        String markup = sw.toString()
        /*
<span>{</span>
<span>"URL.host":</span>
<span>"cdnjs.cloudflare.com"</span>
<span>, </span>
<span>"URL.path":</span>
<span>"/ajax/libs/jquery/1.11.3/jquery.js"</span>
<span>, </span>
<span>"URL.port":</span>
<span>"80"</span>
<span>, </span>
<span>"URL.protocol":</span>
<span>"https"</span>
<span>, </span>
<span>"URL.query":</span>
<span>"q=12345"</span>
<span>, </span>
<span>"profile":</span>
<span>"MyAdmin_ProductionEnv"</span>
<span>}</span>
         */
        println markup
    }

}
