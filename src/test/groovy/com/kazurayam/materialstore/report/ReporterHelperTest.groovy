package com.kazurayam.materialstore.report


import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.report.markupbuilder_templates.MetadataTemplate
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class ReporterHelperTest {

    @Test
    void test_getStyleFromClasspath() {
        String style = ReporterHelper.loadStyleFromClasspath() // https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
        assertNotNull(style)
        //println style
        assertTrue(style.length() > 0)
    }

}
