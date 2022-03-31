package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class MaterialListReporterImpl extends MaterialListReporter {

    private static final Logger logger = LoggerFactory.getLogger(MaterialListReporterImpl.class);

    private final Store store;
    private final JobName jobName;

    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MaterialListBasicReporterTemplate.ftlh";
    // ftlh is a short for "FreeMarker Template Language for HTML"

    private final Configuration cfg;

    public MaterialListReporterImpl(Store store, JobName jobName)
            throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        this.store = store;
        this.jobName = jobName;
        this.cfg = FreeMarkerConfigurator.configureFreeMarker(store);
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
        /* write the resulting HTML into a file*/
        String fileName = (reportFileName == null) ? "list.html" : reportFileName;
        Path filePath = store.getRoot().resolve(fileName);
        this.report(materialList, filePath);
        return filePath;
    }

    @Override
    public void report(MaterialList materialList, Path filePath)
            throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(filePath);
        /* Create a data-model */
        Map<String, Object> model = new HashMap<>();
        model.put("style", StyleHelper.loadStyleFromClasspath());
        model.put("title", getTitle(filePath));
        model.put("filePath", filePath.toString());
        model.put("store", store.getRoot().normalize().toString());
        model.put("model", materialList.toTemplateModel());

        // for debug
        if (isVerboseLoggingEnabled()) {
            writeModel(materialList.toTemplateModelAsJson(true),
                    filePath.getParent());
        }

        /* Get the template */
        Template template;
        try {
            template = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        /* Merge data-model with the template */
        Writer sw = new StringWriter();
        try {
            template.process(model, sw);
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        String html = sw.toString();
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(html, "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            html = doc.toString();
        }

        try {
            Files.write(filePath,
                    html.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }
}
