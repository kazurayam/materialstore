package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.reduce.MProductGroup;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;

import com.kazurayam.materialstore.filesystem.JobName;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
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
 * MProductGroupReporterImplMB re-implemented using FreeMarker.
 *
 */
public class MProductGroupReporterImpl extends MProductGroupReporter {

    private static final Logger logger =
            LoggerFactory.getLogger(MProductGroupReporterImpl.class);

    private final Store store;
    private final JobName jobName;
    private Double criteria = 0.0d;

    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MProductGroupBasicReporterFMTemplate.ftlh";
    // ftlh is a short for "FreeMarker Template Language for HTML"

    private final Configuration cfg;

    public MProductGroupReporterImpl(Store store, JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(jobName);
        this.store = store;
        this.jobName = jobName;
        this.cfg = FreeMarkerConfigurator.configureFreeMarker(store);
    }

    @Override
    public void setCriteria(Double criteria) {
        if (criteria < 0.0 || 100.0 < criteria) {
            throw new IllegalArgumentException(
                    "criteria(${criteria}) must be in the range of [0,100)");
        }
        this.criteria = criteria;
    }

    @Override
    public Path report(MProductGroup mProductGroup, String fileName)
            throws MaterialstoreException {
        Path reportFile = store.getRoot().resolve(fileName);
        this.report(mProductGroup, reportFile);
        return reportFile;
    }

    @Override
    public void report(MProductGroup mProductGroup, Path filePath)
            throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        Objects.requireNonNull(filePath);
        //
        if (! mProductGroup.isReadyToReport()) {
            throw new MaterialstoreException(
                    "given MProductGroup is not ready to report. mProductGroup=" +
                            mProductGroup.toString());
        }
        /* create a data-model */
        Map<String, Object> model = new HashMap<>();
        model.put("style", StyleHelper.loadStyleFromClasspath());
        model.put("title", getTitle(filePath));
        model.put("store", store.getRoot().normalize().toString());
        model.put("mProductGroup", mProductGroup.toTemplateModel());
        model.put("criteria", criteria);

        // for debug
        if (isVerboseLoggingEnabled()) {
            writeModel(mProductGroup.toTemplateModelAsJson(true),
                    filePath.getParent());
        }

        /* Get the template */
        Template template;
        try {
           template = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        /* Merge data-model with template to generate a HTML document*/
        Writer sw = new StringWriter();
        try {
            template.process(model, sw);
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        String html = sw.toString();

        //assert html.contains("</html>s://cdn.");

        /* pretty print the HTML using jsoup if required */
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(html, "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            html = doc.toString();
        }

        try {
            Files.write(filePath, html.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }
}
