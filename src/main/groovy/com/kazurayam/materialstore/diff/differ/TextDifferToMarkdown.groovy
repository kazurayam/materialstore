package com.kazurayam.materialstore.diff.differ

import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function
import java.util.stream.Collectors

/**
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
class TextDifferToMarkdown implements Differ {

    private Path root_

    private Charset charset = StandardCharsets.UTF_8

    TextDifferToMarkdown() {}

    TextDifferToMarkdown(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    void setRoot(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    private static void ensureRoot(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
    }

    void setCharset(Charset chs) {
        Objects.requireNonNull(chs)
        this.charset = chs
    }

    DiffArtifact makeDiff(DiffArtifact input) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(input)
        Objects.requireNonNull(input.getExpected())
        Objects.requireNonNull(input.getActual())
        //
        Material expected = input.getExpected()
        if (! expected.isText()) {
            throw new IllegalArgumentException("is not a text: ${actual}")
        }
        Material actual = input.getActual()
        if (! actual.isText()) {
            throw new IllegalArgumentException("is not a text: ${actual}")
        }

        //
        StringBuilder sb = new StringBuilder()
        sb.append("- original: `${expected.getIndexEntry().getMetadata().toString()}`\n")
        sb.append("- revised:  `${actual.getIndexEntry().getMetadata().toString()}`\n")
        sb.append("\n")
        sb.append(markdownDiff(root_, expected, actual, charset))
        //
        byte[] diffData = toByteArray(sb.toString())
        Metadata diffMetadata = new Metadata([
                "category": "diff",
                "expected": expected.getIndexEntry().getID().toString(),
                "actual": actual.getIndexEntry().getID().toString()
        ])
        Jobber jobber = new Jobber(root_, actual.getJobName(), actual.getJobTimestamp())
        Material diffMaterial = jobber.write(diffData, FileType.MD, diffMetadata)
        //
        DiffArtifact result = new DiffArtifact(expected, actual)
        result.setDiff(diffMaterial)
        return result
    }

    private static String markdownDiff(Path root, Material original, Material revised, Charset charset) {
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

        sb.append((equalRows.size() < rows.size()) ? '**DIFFERENT**' : '**NO DIFF**')
        sb.append("\n\n")

        sb.append("- inserted rows: ${insertedRows.size()}\n")
        sb.append("- deleted rows : ${deletedRows.size()}\n")
        sb.append("- changed rows : ${changedRows.size()}\n")
        sb.append("- equal rows:  : ${equalRows.size()}\n\n")

        sb.append("|line#|original|revised|\n");
        sb.append("|-----|--------|-------|\n");
        rows.eachWithIndex { DiffRow row, index ->
            sb.append("|" + (index+1) + "|" + row.getOldLine() + "|" + row.getNewLine() + "|\n");
        }
        return sb.toString()
    }

    private static List<String> readAllLines(String longText) {
        BufferedReader br = new BufferedReader(new StringReader(longText))
        List<String> lines = new ArrayList<>()
        String line
        while ((line = br.readLine()) != null) {
            lines.add(line)
        }
        return lines
    }

    private static String readMaterial(Path root, Material material, Charset charset) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(material)
        Objects.requireNonNull(charset)
        Jobber jobber = new Jobber(root, material.getJobName(), material.getJobTimestamp())
        byte[] data = jobber.read(material.getIndexEntry())
        return new String(data, charset)
    }

    private static byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8)
    }
}

