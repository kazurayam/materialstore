package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.SortKeys;
import com.kazurayam.materialstore.reduce.MaterialProductGroup;
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
 * MProductGroupReporterImplMB re-implemented using FreeMarker.
 *
 */
public final class MaterialProductGroupReporterImpl extends MaterialProductGroupReporter {

    private static final Logger logger =
            LoggerFactory.getLogger(MaterialProductGroupReporterImpl.class);

    private final Store store;

    private Double criteria = 0.0d;

    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MProductGroupBasicReporterFMTemplate.ftlh";
    // ftlh is a short for "FreeMarker Template Language for HTML"

    private final Configuration cfg;

    public MaterialProductGroupReporterImpl(Store store) throws MaterialstoreException {
        Objects.requireNonNull(store);
        this.store = store;
        this.cfg = FreeMarkerConfigurator.configureFreeMarker(store);
    }

    @Override
    public void report(MaterialProductGroup mpg, Path filePath)
            throws MaterialstoreException {
        this.report(mpg, new SortKeys(), filePath);
    }

    @Override
    public void report(MaterialProductGroup mpg, SortKeys sortKeys, Path filePath)
            throws MaterialstoreException {
        Objects.requireNonNull(mpg);
        Objects.requireNonNull(sortKeys);
        Objects.requireNonNull(filePath);
        if (! mpg.isReadyToReport()) {
            throw new MaterialstoreException(
                    "given MProductGroup is not ready to report. mProductGroup=" +
                            mpg.toString());
        }
        /* sort the entries in the mProductGroup as specified by SortKeys */
        mpg.order(sortKeys);

        /* create a data-model */
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("style", StyleHelper.loadStyleFromClasspath());
        dataModel.put("accordionCustom",
                StyleHelper.loadStyleFromClasspath("/com/kazurayam/materialstore/report/bootstrap-5-accordion-with-an-inline-checkbox.css"));
        dataModel.put("js",
                StyleHelper.loadStyleFromClasspath("/com/kazurayam/materialstore/report/model-manager.js"));
        dataModel.put("title", getTitle(filePath));
        dataModel.put("store", store.getRoot().normalize().toString());
        dataModel.put("mProductGroup", mpg.toTemplateModel(sortKeys));
        dataModel.put("model", mpg.toJson(true));
        dataModel.put("criteria", criteria);
        dataModel.put("sortKeys", sortKeys.toString());

        // for debug
        if (isVerboseLoggingEnabled()) {
            writeModel(mpg.toTemplateModelAsJson(sortKeys,true),
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
            template.process(dataModel, sw);
            sw.flush();
            sw.close();
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        String html;

        /* pretty print the HTML using jsoup if required */
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(sw.toString(), "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            html = doc.toString();
        } else {
            html = sw.toString();
        }

        try {
            Files.write(filePath,
                    html.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public Path report(MaterialProductGroup mpg, SortKeys sortKeys, String fileName)
            throws MaterialstoreException {
        mpg.setCriteria(this.criteria);
        Path reportFile = store.getRoot().resolve(fileName);
        this.report(mpg, sortKeys, reportFile);
        return reportFile;
    }

    @Override
    public Path report(MaterialProductGroup mpg, String fileName)
            throws MaterialstoreException {
        return this.report(mpg, new SortKeys(), fileName);
    }

    @Override
    public void setCriteria(Double criteria) {
        if (criteria < 0.0 || 100.0 < criteria) {
            throw new IllegalArgumentException(
                    "criteria(${criteria}) must be in the range of [0,100)");
        }
        this.criteria = criteria;
    }
}
