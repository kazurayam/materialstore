package com.kazurayam.materialstore.diff.differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.diff.Differ
import com.kazurayam.materialstore.store.Material
import groovy.xml.MarkupBuilder

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
// A problem: how to deal with a long line without white spaces
//  <script  src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.6.4/js/bootstrap-datepicker.min.js"></script>

class TextDifferToHTML extends AbstractTextDiffer implements Differ {

    private static final String OLD_TAG = "|-.-|"
    private static final String NEW_TAG = "|+.+|"

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
                        .oldTag({ f -> OLD_TAG } as Function)
                        .newTag({ f -> NEW_TAG } as Function)
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
            head() {
                meta(charset: "utf-8")
                title("TextDifferToHTML output")
                style(getStyle())
            }
            body() {
                div(id: "container") {
                    div(id: "inputs") {
                        h1("Original")
                        dl() {
                            dt("URL")
                            dd(original.getRelativeURL())
                            dt("metadata")
                            dd(original.getIndexEntry().getMetadata().toString())
                        }
                        h1("Revised")
                        dl() {
                            dt("URL")
                            dd(revised.getRelativeURL())
                            dt("metadata")
                            dd(revised.getIndexEntry().getMetadata().toString())
                        }
                    }
                    h2(id: "decision", ((equalRows.size() < rows.size()) ? 'are DIFFERENT' : 'are EQUAL'))
                    h3("rows")
                    ul(id: "stats") {
                        li() {
                            span("total : ")
                            span(rows.size())
                        }
                        li() {
                            span("inserted : ")
                            span(insertedRows.size())
                        }
                        li() {
                            span("deleted : ")
                            span(deletedRows.size())
                        }
                        li() {
                            span("changed : ")
                            span(changedRows.size())
                        }
                        li() {
                            span("equal : ")
                            span(equalRows.size())
                        }
                    }
                    table() {
                        colgroup() {
                            col(width:"44")
                            col()
                            col()
                        }
                        thead() {
                            tr() {
                                th("")
                                th("original")
                                th("revised")
                            }
                        }
                        tbody() {
                            rows.eachWithIndex { DiffRow row, index ->
                                tr() {
                                    th(index + 1)
                                    td() {
                                        span(class:"blob-code-inner") {
                                            List<String> segments = divideStringIntoSegments(row.getOldLine())
                                            segments.each { segment ->
                                                mb.span(class: "pl", segment)
                                            }
                                        }
                                    }
                                    td() {
                                        span(class:"blob-code-inner") {
                                            List<String> segments = divideStringIntoSegments(row.getNewLine())
                                            segments.each { segment ->
                                                mb.span(class: "pl", segment)
                                            }
                                        }
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
div#container {
    max-width: 1280px;
    margin-left: auto;
    margin-right: auto;
}
table {
    table-layout: fixed;
    border-collapse: collapse;
    border-spacing: 0;
    border: 1px solid #ccc;
    width: 100%;
}
td, th {
    font-size: 12px;
    border-right: 1px solid #ccc;
    display: table-cell;
    
}
th {
    border-bottom: 1px solid #ccc;
}
.blob-code-inner {
    word-wrap: break-word;
    white-space: pre-wrap;
}
.pl {
}
"""
    }

    /**
     * Given:   "    if (! obj instanceof Material) {"
     * returns: "<span class="pl">    if</span><span class="pl"> (!</span><span class="pl"> obj</span><span class="pl"> instanceof</span><span class="pl"> Material)</span><span class="pl"> {</span>"
     *
     * @param line
     * @return
     */
    private static final Pattern SPANNING_PATTERN = Pattern.compile("\\s*\\S+")

    static List<String> divideStringIntoSegments(String line, clazz="pl") {
        Matcher m = SPANNING_PATTERN.matcher(line)
        List<String> segments = m.findAll()
        return segments
    }

}
