package com.kazurayam.materialstore.reduce.differ;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
public final class TextDifferToMarkdown extends AbstractTextDiffer implements Differ {

    public TextDifferToMarkdown(Store store) {
        super(store);
    }

    @Override
    public TextDiffContent makeTextDiffContent(Store store,
                                               final Material original,
                                               final Material revised,
                                               Charset charset) throws MaterialstoreException {
        String originalText = AbstractTextDiffer.readMaterial(store, original, charset);
        String revisedText = AbstractTextDiffer.readMaterial(store, revised, charset);

        //build simple lists of the lines of the two text files
        List<String> originalLines = AbstractTextDiffer.readAllLines(originalText);
        List<String> revisedLines = AbstractTextDiffer.readAllLines(revisedText);

        // Compute the difference between two texts and print it in human-readable markup style
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(true)
                        .oldTag(f -> "*")
                        .newTag(f -> "**")
                        .build();

        List<DiffRow> rows = generator.generateDiffRows(originalLines, revisedLines);

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

        // print the diff info in Markdown format into a file out.md
        final StringBuilder sb = new StringBuilder();

        sb.append("- original: `" + original.getIndexEntry().getMetadata().toString() + "`\n");
        sb.append("- revised:  `" + revised.getIndexEntry().getMetadata().toString() + "`\n");
        sb.append("\n");

        sb.append((equalRows.size() < rows.size()) ? "**DIFFERENT**" : "**NO DIFF**");
        sb.append("\n\n");

        sb.append("- inserted rows: " + insertedRows.size() + "\n");
        sb.append("- deleted rows : " + deletedRows.size() + "\n");
        sb.append("- changed rows : " + changedRows.size() + "\n");
        sb.append("- equal rows:  : " + equalRows.size() + "\n\n");

        sb.append("| line# | original | revised |\n");
        sb.append("| ----: | :------- | :------ |\n");
        IntStream.range(0,rows.size())
                        .forEach(index -> {
                            DiffRow row = rows.get(index);
                            sb.append("| " + (index + 1) + " | " + row.getOldLine() + " | " + row.getNewLine() + " |\n");
                        });

        return new TextDiffContent.Builder(sb.toString())
                .inserted(insertedRows.size())
                .deleted(deletedRows.size())
                .changed(changedRows.size())
                .equal(equalRows.size())
                .build();
    }

}
