package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.MaterialstoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReporterHelperTest {

    @Test
    public void test_getStyleFromClasspath() throws MaterialstoreException {
        String style = StyleHelper.loadStyleFromClasspath(); // https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
        Assertions.assertNotNull(style);
        //println style
        Assertions.assertTrue(!style.isEmpty());
    }

}
