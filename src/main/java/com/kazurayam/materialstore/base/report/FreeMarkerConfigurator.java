package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.freemarker.CompressToSingleLineDirective;
import com.kazurayam.materialstore.freemarker.ReadAllLinesDirective;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

public final class FreeMarkerConfigurator {

    public static Configuration configureFreeMarker(Store store)
            throws MaterialstoreException {
        // create and adjust the configuration singleton
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(
                MaterialListReporterImpl.class.getClassLoader(),
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
            cfg.setSharedVariable("baseDir", store.getRoot().normalize().toAbsolutePath().toString());
            cfg.setSharedVariable("compressToSingleLine",
                    new CompressToSingleLineDirective());
        } catch (TemplateModelException e) {
            throw new MaterialstoreException(e);
        }

        return cfg;
    }

}
