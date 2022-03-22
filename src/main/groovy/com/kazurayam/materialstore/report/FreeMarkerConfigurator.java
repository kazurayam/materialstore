package com.kazurayam.materialstore.report;

import com.kazurayam.freemarker.ReadAllLinesDirective;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

public class FreeMarkerConfigurator {

    static Configuration configureFreeMarker(Store store)
            throws MaterialstoreException {
        // create and adjust the configuration singleton
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(
                MaterialListBasicReporter.class.getClassLoader(),
                "freemarker_templates"
        ));
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);

        // user-defined directives
        try {
            cfg.setSharedVariable("readAllLines",
                    new ReadAllLinesDirective());
            cfg.setSharedVariable("store",
                    store.getRoot().normalize().toAbsolutePath().toString());
        } catch (TemplateModelException e) {
            throw new MaterialstoreException(e);
        }

        return cfg;
    }

}
