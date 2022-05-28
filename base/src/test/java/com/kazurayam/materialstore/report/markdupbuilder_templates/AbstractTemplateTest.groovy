package com.kazurayam.materialstore.report.markdupbuilder_templates

import com.kazurayam.materialstore.filesystem.Metadata
import org.junit.jupiter.api.BeforeAll

abstract class AbstractTemplateTest {


    static String url0 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.js?q=12345"
    static String url1 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.js?q=67890"
    static String urlT = "http://myadmin.kazurayam.com/"
    static Metadata metadata0
    static Metadata metadata1
    static Metadata metadataT

    @BeforeAll
    static void beforeAll() {
        metadata0 = Metadata.builder(new URL(url0))
                .put("profile", "MyAdmin_ProductionEnv").build()
        metadata1 = Metadata.builder(new URL(url1))
                .put("profile", "MyAdmin_DevelopmentEnv").build()
        metadataT = Metadata.builder(new URL(urlT))
                .put("profile", "MyAdmin_ProductionEnv").build()
    }

}
