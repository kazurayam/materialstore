package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MaterialListBasicReportFM class is coded in Java, not in Groovy.
 *
 * MaterialListBasicReportFM uses FreeMarker as the HTML template engine.
 *
 */
public class MaterialListBasicReporterFM extends MaterialListReporter {

    private static final Logger logger = LoggerFactory.getLogger(MaterialListBasicReporterFM.class);

    private final Store store;
    private final JobName jobName;

    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MaterialListReportTemplate.ftlh";
    // ftlh is a short for FreeMarker Template Language for HTML

    private final Configuration cfg;

    public MaterialListBasicReporterFM(Store store, JobName jobName) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        this.store = store;
        this.jobName = jobName;
        this.cfg = configureFreeMarker();
    }

    private Configuration configureFreeMarker() {
        // create and adjust the configuration singleton
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(
                MaterialListBasicReporterFM.class.getClassLoader(),
                "freemarker_templates"
        ));
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        return cfg;
    }

    /**
     * using Bootstrap 5
     * using FreeMarker
     *
     * @param materialList List of MaterialList object to print
     * @param reportFileName "list.html" as default
     * @return Path object as the output
     */
    @Override
    public Path report(MaterialList materialList, String reportFileName)
            throws MaterialstoreException {
        Objects.requireNonNull(materialList);

        /* Create a data-model */
        Map<String, Object> model = new HashMap<>();
        model.put("user", "Big Joe");

        /* Get the template */
        Template temp;
        try {
            temp = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        /* Merge data-model with template */
        Writer sw = new StringWriter();
        try {
            temp.process(model, sw);
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        /* write the resulting HTML into a file*/
        String fileName = (reportFileName == null) ? "list.html" : reportFileName;
        Path reportFile = store.getRoot().resolve(fileName);
        try {
            Files.write(reportFile,
                    sw.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        return reportFile;
    }
}
