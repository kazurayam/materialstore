package com.kazurayam.materialstore.freemarker;

import com.kazurayam.materialstore.TestOutputOrganizerFactory;
import com.kazurayam.unittest.TestOutputOrganizer;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestBase {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(TestBase.class);
    protected Configuration cfg;
    protected Map<String, Object> model;

    public TestBase() throws IOException {
        Path projectDir = too.getProjectDir();
        /* ---------------------------------------------------------- */
        /* You should do this ONLY ONCE in the whole application lifecycle */

        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_31);

        Path templatesDir = projectDir.resolve("src/test/resources/freemarker_templates");
        cfg.setDirectoryForTemplateLoading(templatesDir.toFile());

        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setBooleanFormat("c");
        cfg.setOutputEncoding("UTF-8");

        // add custom directives
        try {
            cfg.setSharedVariable("readAllLines", new ReadAllLinesDirective());
            Path store = projectDir.resolve("src/test/fixtures/freemarker").resolve("store");
            cfg.setSharedVariable("baseDir", store.normalize().toAbsolutePath().toString());
            //
            cfg.setSharedVariable("uppercase", new UpperCaseDirective());
            //
            cfg.setSharedVariable("compressToSingleLine", new CompressToSingleLineDirective());

        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }

        /* ---------------------------------------------------------- */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle: */

        /* Create a data-model */
        model = new HashMap<>();
    }
}
