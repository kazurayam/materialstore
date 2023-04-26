package com.kazurayam.materialstore.base.reduce.differ;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.kazurayam.materialstore.base.report.FreeMarkerConfigurator;
import com.kazurayam.materialstore.base.report.HTMLPrettyPrintingCapable;
import com.kazurayam.materialstore.base.report.StyleHelper;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * compiles a HTML report of diff of 2 text files
 * presents the diff information in a HTML like the GitHub History split view.
 * <p>
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
public final class TextDifferToHTML extends AbstractTextDiffer implements Differ, HTMLPrettyPrintingCapable {

    private static final Logger logger = LoggerFactory.getLogger(TextDifferToHTML.class);

    private boolean prettyPrinting = false;

    public TextDifferToHTML(Store store) {
        super(store);
    }

    @Override
    public void enablePrettyPrinting(boolean enabled) {
        this.prettyPrinting = enabled;
    }

    @Override
    public boolean isPrettyPrintingEnabled() {
        return prettyPrinting;
    }

    @Override
    public TextDiffContent makeTextDiffContent(Store store,
                                               final Material left,
                                               final Material right,
                                               Charset charset)
            throws MaterialstoreException {
        String leftText = readMaterial(store, left, charset);
        String rightText = readMaterial(store, right, charset);

        //build simple lists of the lines of the two text files
        List<String> leftLines = readAllLines(leftText);
        List<String> rightLines = readAllLines(rightText);

        // Compute the difference between two texts and print it in human-readable markup style
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(true)
                        .oldTag(f -> OLD_TAG)
                        .newTag(f -> NEW_TAG)
                        .lineNormalizer(str ->
                                str.replaceAll("&lt;", "<")
                                .replaceAll("&gt;", ">")
                                .replaceAll("&quot;", "\"")
                                .replaceAll("&apos;", "'")
                                .replaceAll("&amp;", "&"))
                        .build();

        final List<DiffRow> rows = generator.generateDiffRows(leftLines, rightLines);

        final List<DiffRow> insertedRows =
                rows.stream()
                        .filter( dr -> dr.getTag().equals(DiffRow.Tag.INSERT))
                        .collect(Collectors.toList());

        final List<DiffRow> deletedRows =
                rows.stream()
                        .filter( dr -> dr.getTag().equals(DiffRow.Tag.DELETE))
                        .collect(Collectors.toList());

        final List<DiffRow> changedRows =
                rows.stream()
                        .filter( dr -> dr.getTag().equals(DiffRow.Tag.CHANGE))
                        .collect(Collectors.toList());

        final List<DiffRow> equalRows =
                rows.stream()
                        .filter( dr -> dr.getTag().equals(DiffRow.Tag.EQUAL))
                        .collect(Collectors.toList());

        Double diffRatio = DifferUtil.roundUpTo2DecimalPlaces(
                (insertedRows.size() +
                        deletedRows.size() +
                        changedRows.size()
                ) * 100.0D / rows.size()
        );
        String ratio = DifferUtil.formatDiffRatioAsString(diffRatio);


        Map<String, Object> model = new HashMap<>();
        model.put("rowsSize", rows.size());
        model.put("insertedRowsSize", insertedRows.size());
        model.put("deletedRowsSize", deletedRows.size());
        model.put("changedRowsSize", changedRows.size());
        model.put("equalRowsSize", equalRows.size());
        model.put("ratio", ratio);
        model.put("style", StyleHelper.loadStyleFromClasspath("/com/kazurayam/materialstore/base/reduce/differ/style.css"));
        model.put("title", "TextDifferToHTML output");
        Map<String, String> leftData = new HashMap<String, String>() {{
            put("relativeURL", left.getRelativeURL());
            put("fileType", left.getFileType().getExtension());
            put("metadata", left.getMetadata().toString());
            put("url", left.getMetadata().toURLAsString());
        }};
        Map<String, String> rightData = new HashMap<String, String>() {{
            put("relativeURL", right.getRelativeURL());
            put("fileType", right.getFileType().getExtension());
            put("metadata", right.getMetadata().toString());
            put("url", right.getMetadata().toURLAsString());
        }};
        model.put("leftData", leftData);
        model.put("rightData", rightData);

        List<Map<String, Object>> rowsAsModel = new ArrayList<>();
        int count = 1;
        for (DiffRow row : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("index", count);
            m.put("class", getClassOfDiffRow(row));
            m.put("left_segments", splitStringWithOldNewTags(row.getOldLine()));
            m.put("right_segments", splitStringWithOldNewTags(row.getNewLine()));
            rowsAsModel.add(m);
            //
            count += 1;
        }
        logger.debug("#makeTextDiffContent rowsAsModel.size()=" + rowsAsModel.size());
        model.put("rows", rowsAsModel);
        model.put("OLD_TAG", OLD_TAG);
        model.put("NEW_TAG", NEW_TAG);

        // compile the report content
        String content = makeContentString(model);
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(content, "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            content = doc.toString();
        }

        return new TextDiffContent.Builder(content)
                        .inserted(insertedRows.size())
                        .deleted(deletedRows.size())
                        .changed(changedRows.size())
                        .equal(equalRows.size())
                        .build();
    }

    private String makeContentString(Map<String, Object> model)
            throws MaterialstoreException {
        // Configure FreeMarker
        Configuration cfg = FreeMarkerConfigurator.configureFreeMarker(store);

        // Get the template
        Template template;
        try {
            template = cfg.getTemplate(
                    "com/kazurayam/materialstore/reduce/differ/TextDifferToHTMLTemplate.ftlh");
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        // Merge data-model with the template
        Writer sw = new StringWriter();
        try {
            template.process(model, sw);
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        return sw.toString();
    }

}
