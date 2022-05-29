package com.kazurayam.materialstore.report;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class TextDiffUtil {

    /**
     * Compare 2 lists of strings to write a diff in Markdown format into a file.
     * 1. will ignore indenting whitespaces and trailing whitespaces
     * 2. will ignore empty lines
     *
     * @param original
     * @param revised
     * @param file
     * @param ignoreLinesContaining
     * @throws FileNotFoundException
     */
    public static void writeDiff(List<String> original, List<String> revised, Path file,
                                 List<String> ignoreLinesContaining)
            throws FileNotFoundException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "*")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                original,
                revised);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file.toFile()), StandardCharsets.UTF_8));
        pw.println("|Line#|original|new|");
        pw.println("|-----|--------|---|");
        for (int i = 0; i < rows.size(); i++) {
            DiffRow row = rows.get(i);
            // we will ignore the difference due to indenting white spaces
            String oldLine = row.getOldLine();
            String newLine = row.getNewLine();
            if ( !oldLine.equals(newLine) ) {
                if (shouldBeIgnored(oldLine, ignoreLinesContaining) ||
                        shouldBeIgnored(newLine, ignoreLinesContaining)) {
                    ;
                } else {
                    pw.println("|" + i + "|" + oldLine + "|" + newLine + "|");
                }
            }
        }
        pw.flush();
        pw.close();
    }

    static boolean shouldBeIgnored(String line, List<String> ignoreLinesContaining) {
        for (String pattern : ignoreLinesContaining) {
            if (pattern.length() > 0 && line.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
