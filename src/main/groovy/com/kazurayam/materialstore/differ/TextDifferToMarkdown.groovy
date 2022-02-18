package com.kazurayam.materialstore.differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.filesystem.Material

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.function.Function
import java.util.stream.Collectors

/**
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
final class TextDifferToMarkdown extends AbstractTextDiffer implements Differ {

    TextDifferToMarkdown() {
        super()
    }

    TextDifferToMarkdown(Path root) {
        super(root)
    }

    @Override
    TextDiffContent makeContent(Path root, Material original, Material revised, Charset charset) {
        String originalText = readMaterial(root, original, charset)
        String revisedText = readMaterial(root, revised, charset)

        //build simple lists of the lines of the two text files
        List<String> originalLines = readAllLines(originalText);
        List<String> revisedLines = readAllLines(revisedText);

        // Compute the difference between two texts and print it in humann-readable markup style
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(true)
                        .oldTag({ f -> "*" } as Function)
                        .newTag({ f -> "**" } as Function)
                        .build()

        List<DiffRow> rows = generator.generateDiffRows(originalLines, revisedLines)

        List<DiffRow> insertedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.INSERT
                }).collect(Collectors.toList())

        List<DiffRow> deletedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.DELETE
                }).collect(Collectors.toList())

        List<DiffRow> changedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.CHANGE
                }).collect(Collectors.toList())

        List<DiffRow> equalRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.EQUAL
                }).collect(Collectors.toList())

        // print the diff info in Markdown format into a file out.md
        StringBuilder sb = new StringBuilder()

        sb.append("- original: `${original.getIndexEntry().getMetadata().toString()}`\n")
        sb.append("- revised:  `${revised.getIndexEntry().getMetadata().toString()}`\n")
        sb.append("\n")

        sb.append((equalRows.size() < rows.size()) ? '**DIFFERENT**' : '**NO DIFF**')
        sb.append("\n\n")

        sb.append("- inserted rows: ${insertedRows.size()}\n")
        sb.append("- deleted rows : ${deletedRows.size()}\n")
        sb.append("- changed rows : ${changedRows.size()}\n")
        sb.append("- equal rows:  : ${equalRows.size()}\n\n")

        sb.append("| line# | original | revised |\n");
        sb.append("| ----: | :------- | :------ |\n");
        rows.eachWithIndex { DiffRow row, index ->
            sb.append("| " + (index+1) + " | " + row.getOldLine() + " | " + row.getNewLine() + " |\n");
        }

        return new TextDiffContent.Builder(sb.toString())
                .inserted(insertedRows.size())
                .deleted(deletedRows.size())
                .changed(changedRows.size())
                .equal(equalRows.size())
                .build()
    }

}

