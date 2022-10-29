package com.kazurayam.materialstore.base.report;

import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
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
 * MaterialListBasicReportFM class is coded in Java.
 *
 * MaterialListBasicReportFM uses FreeMarker as the HTML template engine.
 *
 */
public final class MaterialListReporterImpl extends MaterialListReporter {

    private static final Logger logger = LoggerFactory.getLogger(MaterialListReporterImpl.class);

    private final Store store;

    private static String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MaterialListBasicReporterTemplate.ftlh";
    // ftlh is a short for "FreeMarker Template Language for HTML"

    private final Configuration cfg;

    public MaterialListReporterImpl(Store store)
            throws MaterialstoreException {
        Objects.requireNonNull(store);
        this.store = store;
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
        return this.report(materialList, new SortKeys(), reportFileName);
    }

    @Override
    public Path report(MaterialList materialList, SortKeys sortKeys, String reportFileName)
            throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(sortKeys);
        String fileName = (reportFileName == null) ? "list.html" : reportFileName;
        /* write the resulting HTML into a file */
        Path filePath = store.getRoot().resolve(fileName);
        this.report(materialList, sortKeys, filePath);
        return filePath;
    }

    @Override
    public void report(MaterialList materialList, Path filePath)
            throws MaterialstoreException {
        this.report(materialList, new SortKeys(), filePath);
    }
    @Override
    public void report(MaterialList materialList, SortKeys sortKeys, Path filePath)
            throws MaterialstoreException {
        Objects.requireNonNull(materialList);
        Objects.requireNonNull(sortKeys);
        Objects.requireNonNull(filePath);

        // sort the entries by the specified keys
        materialList.order(sortKeys);

        /* Create a data-model */
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("style", StyleHelper.loadStyleFromClasspath());
        dataModel.put("style2", StyleHelper.loadStyleFromClasspath(
                "/com/kazurayam/materialstore/base/reduce/differ/style.css"));
        dataModel.put("title", getTitle(filePath));
        dataModel.put("filePath", filePath.toString());
        dataModel.put("store", store.getRoot().normalize().toString());

        dataModel.put("model", materialList.toTemplateModel(sortKeys));
        dataModel.put("sortKeys", sortKeys.toString());

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

        /* Merge data-dataModel with the template */
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
}
