package com.kazurayam.materialstore.diff.differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.diff.DiffArtifact
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.store.FileType
import com.kazurayam.materialstore.store.Jobber
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.Metadata
import groovy.xml.MarkupBuilder

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
class TextDifferToHTML extends AbstractTextDiffer implements Differ {

    TextDifferToHTML(Path root) {
        super(root)
    }

    @Override
    String makeContent(Path root, Material original, Material revised, Charset charset) {
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
                        .oldTag({ f -> "|-|" } as Function)
                        .newTag({ f -> "|+|" } as Function)
                        .lineNormalizer({str ->
                                str.replaceAll("&lt;", "<")
                                        .replaceAll("&gt;",">")
                        })
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

        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.html(lang:"en") {
            mb.head() {
                meta(charset: "utf-8")
                title("TextDifferToHTML output")
                style(getStyle())
            }
            mb.body() {
                mb.div(id: "diff") {
                    ul(id: "inputs") {
                        li() {
                            span("original")
                            span(original.getIndexEntry().getMetadata().toString())
                        }
                        li() {
                            span("revised")
                            span(revised.getIndexEntry().getMetadata().toString())
                        }
                    }
                    p(id: "decision", ((equalRows.size() < rows.size()) ? 'DIFFERENT' : 'EQUALS'))
                    ul(id: "stats") {
                        li() {
                            span("total rows")
                            span(rows.size())
                        }
                        li() {
                            span("inserted rows")
                            span(insertedRows.size())
                        }
                        li() {
                            span("deleted rows")
                            span(deletedRows.size())
                        }
                        li() {
                            span("changed rows")
                            span(changedRows.size())
                        }
                        li() {
                            span("equal rows")
                            span(equalRows.size())
                        }
                    }
                    table() {
                        thead() {
                            tr() {
                                th("line#")
                                th("original")
                                th("revised")
                            }
                        }
                        tbody() {
                            rows.eachWithIndex { DiffRow row, index ->
                                tr() {
                                    td(index + 1)
                                    td() {
                                        span(row.getOldLine())
                                    }
                                    td() {
                                        span(row.getNewLine())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sw.toString()
    }

    private static String getStyle() {
        return """
* {
    box-sizing: border-box;
}
body {
    font-family: ui-monospace,SFMono-Regular,SZ Mono,
        Menlo,Consolas,Liberation Mono,monospace;
    font-size: 12px;
    line-height: 20px;
}
div#diff {
    width: 90%;
}
table {
    border-collapse: collapse;
    border-spacing: 0;
}
td, th {
    font-size: 12px;
}
"""
    }


}

